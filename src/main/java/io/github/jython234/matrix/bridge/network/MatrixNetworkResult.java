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
package io.github.jython234.matrix.bridge.network;

import io.github.jython234.matrix.bridge.network.error.MatrixErrorResponse;

/**
 * Represents the result of a network operation for Matrix. If
 * <code>successful</code> is true, then there will be a returned payload.
 * If not, then <code>error</code> will contain error information as to
 * why the request failed.
 *
 * @param <T> The type of payload or returned value that the network operation will return.
 */
public class MatrixNetworkResult<T> {
    /**
     * If the request was successful or not.
     */
    public final boolean successful;
    /**
     * The HTTP status code of the response.
     */
    public final int statusCode;

    /**
     * The returned value of the response, which differs on what the operation was. For
     * some it may be <code>null</code>, others it may not.
     *
     * If {@link #successful} is <code>false</code>, then this will be <code>null</code>.
     * See {@link #error} for the error information.
     *
     * For example, if you query the homeserver for a list of room members, then this will contain
     * the list of room members. The method you use for the specific operation will have more detail
     * on what this will contain.
     */
    public final T result;
    /**
     * Error information returned by the homeserver as to why the request failed.
     *
     * If {@link #successful} is <code>true</code> then this will be <code>null</code>.
     */
    public final MatrixErrorResponse error;

    public MatrixNetworkResult(int statusCode, MatrixErrorResponse error) {
        this.successful = false;
        this.statusCode = statusCode;
        this.result = null;
        this.error = error;
    }

    public MatrixNetworkResult(int statusCode, T result) {
        this.successful = true;
        this.statusCode = statusCode;
        this.result = result;
        this.error = null;
    }

    public MatrixNetworkResult(boolean successful, int statusCode, T result, MatrixErrorResponse error) {
        this.successful = successful;
        this.statusCode = statusCode;
        this.result = result;
        this.error = error;
    }
}
