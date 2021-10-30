package com.jslib.injector;

public class ProvisionException extends RuntimeException
{
  private static final long serialVersionUID = 97921102233089131L;

  public ProvisionException()
  {
    super();
  }

  public ProvisionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
  {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public ProvisionException(String message, Throwable cause)
  {
    super(message, cause);
  }

  public ProvisionException(String message)
  {
    super(message);
  }

  public ProvisionException(Throwable cause)
  {
    super(cause);
  }

  public ProvisionException(String message, Object... arguments)
  {
    super(String.format(message, arguments));
  }
}
