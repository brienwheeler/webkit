/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2018 Brien L. Wheeler (brienwheeler@yahoo.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.brienwheeler.lib.util;

/**
 * Error thrown when a validation assertion is violated.
 * 
 * @author Brien Wheeler
 */
public class ValidationException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    /**
     * Construct a {@link ValidationException} with no message detail or
     * underlying cause
     */
    public ValidationException()
    {
        super();
    }

    /**
     * Construct a {@link ValidationException} with a message detail
     *
     * @param message the exception message
     */
    public ValidationException(String message)
    {
        super(message);
    }

    /**
     * Construct a {@link ValidationException} with a message detail and an
     * underlying cause
     *
     * @param message the exception message
     * @param cause the underlying cause
     */
    public ValidationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Construct a {@link ValidationException} with an underlying cause
     *
     * @param cause the underlying cause
     */
    public ValidationException(Throwable cause)
    {
        super(cause);
    }
}
