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

import jdk.incubator.http.HttpResponse;

/**
 * Represents the result of a network operation for Matrix.
 *
 * @author jython234
 */
public class MatrixNetworkResult<T> {
    /**
     * If the operation was successful or not.
     */
    public final boolean successful;

    /**
     * The raw HTTP response of the operation.
     */
    public final HttpResponse<String> httpResponse;

    /**
     * If the operation was not successful then this will contain
     * the error information returned by the server.
     *
     * If the operation was successful then this will be <code>null</code>
     *
     * @see #successful
     */
    public final MatrixErrorResponse error;

    /**
     * If the operation returned anything then this will contain that.
     *
     * For example, in {@link MatrixUserClient#getAvatarURL()} this would contain the Avatar URL.
     */
    public final T result;

    public MatrixNetworkResult(boolean successful, HttpResponse<String> response, MatrixErrorResponse errorResponse, T result) {
        this.successful = successful;
        this.httpResponse = response;
        this.error = errorResponse;
        this.result = result;
    }

    public MatrixNetworkResult(boolean successful, HttpResponse<String> response, T result) {
        this(successful, response, successful ? null : MatrixClientManager.gson.fromJson(response.body(), MatrixErrorResponse.class), result);
    }
}
