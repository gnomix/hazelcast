/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.ascii.memcache;

import com.hazelcast.ascii.TextCommandServiceImpl;
import com.hazelcast.logging.ILogger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Level;

/**
 * User: sancar
 * Date: 3/8/13
 * Time: 2:03 PM
 */
public class TouchCommandProcessor extends MemcacheCommandProcessor<TouchCommand> {

    private final ILogger logger;

    public TouchCommandProcessor(TextCommandServiceImpl textCommandService) {
        super(textCommandService);
        logger = textCommandService.getNode().getLogger(this.getClass().getName());
    }

    public void handle(TouchCommand touchCommand) {
        String key = null;
        try {
            key = URLDecoder.decode(touchCommand.getKey(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        String mapName = DefaultMapName;
        int index = key.indexOf(':');
        if (index != -1) {
            mapName = MapNamePreceder + key.substring(0, index);
            key = key.substring(index + 1);
        }
        int ttl = textCommandService.getAdjustedTTLSeconds(touchCommand.getExpiration());
        textCommandService.lock(mapName, key);
        final Object value = textCommandService.get(mapName, key);
        textCommandService.incrementTouchCount();
        if (value != null) {
            textCommandService.put(mapName, key, value, ttl);
            touchCommand.setResponse(TOUCHED);
        } else {
            touchCommand.setResponse(NOT_STORED);
        }
        textCommandService.unlock(mapName, key);

        if (touchCommand.shouldReply()) {
            textCommandService.sendResponse(touchCommand);
        }
    }

    public void handleRejection(TouchCommand request) {
        request.setResponse(NOT_STORED);
        if (request.shouldReply()) {
            textCommandService.sendResponse(request);
        }
    }
}