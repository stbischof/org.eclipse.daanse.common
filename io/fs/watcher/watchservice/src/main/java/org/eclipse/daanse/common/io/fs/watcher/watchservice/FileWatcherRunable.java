/*
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   SmartCity Jena - initial
*   Stefan Bischof (bipolis.org) - initial
*/
package org.eclipse.daanse.common.io.fs.watcher.watchservice;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.daanse.common.io.fs.watcher.api.EventKind;
import org.eclipse.daanse.common.io.fs.watcher.api.FileSystemWatcherListener;
import org.eclipse.daanse.common.io.fs.watcher.api.FileSystemWatcherWhiteboardConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FileWatcherRunable implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileWatcherRunable.class);

    private FileSystem fileSystem;
    private WatchService watchService;

    private final Map<WatchKey, WatchKeyConfig> watchKeysToConfig = new ConcurrentHashMap<>();

    final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    private boolean stop;

    public FileWatcherRunable() throws IOException {

        fileSystem = FileSystems.getDefault();
        watchService = fileSystem.newWatchService();

    }

    @Override
    public void run() {

        loopWatchUntilStop();

        shutdown();
    }

    void addFileWatcherRunable(FileSystemWatcherListener listener, Map<String, Object> props) throws IOException {

        Object oRecursive = props.getOrDefault(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_RECURSIVE,
                "false");
        if (oRecursive == null) {
            oRecursive = "false";
        }

        boolean recursive = Boolean.parseBoolean(oRecursive.toString());

        Object objPattern = props.getOrDefault(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_PATTERN,
                FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_PATTERN_DEFAULT);

        boolean emptyPattern = objPattern == null || objPattern.toString().isBlank();
        Optional<Pattern> oPattern = emptyPattern ? Optional.empty()
                : Optional.of(Pattern.compile(objPattern.toString()));

        Object oKinds = props.getOrDefault(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_KINDS,
                new String[] { EventKind.ENTRY_CREATE.toString(), EventKind.ENTRY_DELETE.toString(),
                        EventKind.ENTRY_MODIFY.toString() });

        List<Kind<?>> kinds = new ArrayList<>();

        if (oKinds instanceof String[] sKinds) {

            for (int i = 0; i < sKinds.length; i++) {

                String sKind = sKinds[i];
                if ("ENTRY_CREATE".equals(sKind)) {

                    kinds.add(EventKind.ENTRY_CREATE.getKind());
                } else if ("ENTRY_DELETE".equals(sKind)) {

                    kinds.add(EventKind.ENTRY_DELETE.getKind());
                } else if ("ENTRY_MODIFY".equals(sKind)) {

                    kinds.add(EventKind.ENTRY_MODIFY.getKind());
                }
            }
        }

        Object oPath = props.getOrDefault(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_PATH, "");

        boolean emptyPath = oPath == null || oPath.toString().isBlank();
        String sPath = emptyPath ? "" : oPath.toString();
        Path observedPath = fileSystem.getPath(sPath).toAbsolutePath();

        listener.handleBasePath(observedPath);
        WatchKeyConfig config = new WatchKeyConfig(listener, observedPath, List.copyOf(kinds), oPattern, recursive);

        if (recursive) {
            registerPathWithSubDirs(config);
        } else {
            registerPath(config);
        }

    }

    void remove(FileSystemWatcherListener listener) {

        watchKeysToConfig.entrySet().stream().filter(e -> listener.equals(e.getValue().listener())).map(Entry::getKey)
                .forEach(this::unregistertKey);

    }

    private void registerPath(WatchKeyConfig config) throws IOException {

        Path path = config.path();
        try (Stream<Path> stream = Files.list(path)) {
            List<Path> currentPaths = stream.toList();
            FileSystemWatcherListener listener = config.listener();
            listener.handleInitialPaths(currentPaths);
        }

        rwl.writeLock().lock();
        WatchKey watchKey = path.register(watchService, kindsListToArray(config));
        watchKeysToConfig.put(watchKey, config);
        rwl.writeLock().unlock();

    }

    private static Kind<?>[] kindsListToArray(WatchKeyConfig config) {
        return config.kinds().toArray(new Kind<?>[config.kinds().size()]);
    }

    public void shutdown() {
        stop = true;
        synchronized (watchKeysToConfig) {
            for (WatchKey key : watchKeysToConfig.keySet()) {
                key.cancel();
            }
            watchKeysToConfig.clear();
        }
        try {
            if (watchService != null) {
                watchService.close();
                watchService = null;

            }
        } catch (IOException e) {
            LOGGER.error("Exception while WatcheService::close on shutdown", e);
        }
    }

    private void loopWatchUntilStop() {
        try {
            while (!stop) {
                WatchKey key = watchService.take();

                if (key == null) {
                    // no events, try again
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {

                    rwl.readLock().lock();
                    WatchKeyConfig config = watchKeysToConfig.get(key);
                    rwl.readLock().unlock();

                    handleEvent(event, config);

                }

                boolean resetValid = key.reset();
                if (!resetValid) {
                    LOGGER.warn("invalid WatchKey while reset.  watchable: {}", key.watchable());
                }
            }
        } catch (

        ClosedWatchServiceException e) {
            LOGGER.warn("watcheService::take is interrupted, by closing WatchService");
        } catch (InterruptedException e) {
            shutdown();
            Thread.currentThread().interrupt();
            LOGGER.error("watcheService::take is interrupted for unknown reason", e);
        }
    }

    private void handleEvent(WatchEvent<?> event, WatchKeyConfig config) {
        WatchEvent.Kind<?> kind = event.kind();
        if (kind == StandardWatchEventKinds.OVERFLOW) {
            return;// not registerable
        }

        if (!(event.context() instanceof Path)) {
            return; // not an WatchEvent<Path>
        }

        @SuppressWarnings("unchecked")
        WatchEvent<Path> watchEvent = (WatchEvent<Path>) event;

        Path filename = watchEvent.context();

        Path resolvedFile = config.path().resolve(filename);

        if (config.recursive() && (kind == StandardWatchEventKinds.ENTRY_CREATE)) {
            try {
                if (Files.isDirectory(resolvedFile, LinkOption.NOFOLLOW_LINKS)) {
                    registerPathWithSubDirs(new WatchKeyConfig(config.listener(), resolvedFile, config.kinds(),
                            config.oPattern(), config.recursive()));
                }
            } catch (IOException e) {
                LOGGER.error("Exception while register Path with sub-dirs", e);
            }
        }

        config.oPattern().ifPresent(pattern -> {
            Matcher matcher = pattern.matcher(resolvedFile.toString());
            if (matcher.matches()) {
                config.listener().handlePathEvent(resolvedFile, watchEvent.kind());
            }
        });

    }

    private void registerPathWithSubDirs(WatchKeyConfig config) throws IOException {
        Files.walkFileTree(config.path(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path currentDirectory, BasicFileAttributes attrs)
                    throws IOException {
                registerPath(new WatchKeyConfig(config.listener(), currentDirectory, config.kinds(), config.oPattern(),
                        config.recursive()));
                return FileVisitResult.CONTINUE;
            }
        });

    }

    private void unregistertKey(WatchKey watchKey) {
        watchKey.cancel();
        watchKeysToConfig.remove(watchKey);
    }
}
