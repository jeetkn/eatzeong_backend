package com.place.exception;

public class ExistException extends Exception {
    public ExistException()
    {
    }

    public ExistException(String message)
    {
        super(message);
    }

    public ExistException(Throwable cause)
    {
        super(cause);
    }

    public ExistException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ExistException(String message, Throwable cause,
                           boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
