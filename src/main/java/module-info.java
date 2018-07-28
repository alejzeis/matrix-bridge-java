/*
 * Copyright © 2018, jython234
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
module matrixjava.bridge {
    // JDK
    requires java.base;
    requires jdk.incubator.httpclient;

    // Matrix Appservice Java
    requires matrixjava.appservice;

    // Logging
    requires slf4j.api;

    // YAML
    requires snakeyaml;

    // GSON
    requires gson;

    // Apache Commons IO
    requires commons.io;

    // MongoDB
    requires bson;
    requires org.mongodb.driver.core;
    requires org.mongodb.driver.async.client;

    // Spring/Spring Boot
    requires spring.beans;
    requires spring.core;
    requires spring.context;
    requires spring.web;
    requires spring.webmvc;
    requires spring.boot;
    requires spring.boot.autoconfigure;

    // Exports
    exports io.github.jython234.matrix.bridge;
    exports io.github.jython234.matrix.bridge.configuration;
    exports io.github.jython234.matrix.bridge.db;
    exports io.github.jython234.matrix.bridge.event;
    exports io.github.jython234.matrix.bridge.event.core;
    exports io.github.jython234.matrix.bridge.network;
    exports io.github.jython234.matrix.bridge.network.error;
    //exports io.github.jython234.matrix.bridge.network.request;
    exports io.github.jython234.matrix.bridge.network.response;
}
