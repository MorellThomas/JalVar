package jalview.io.exceptions;

/**
 * wrapper for passing error messages and exceptions back to UI when image io goes wrong
 * @author jprocter
 *
 */
public class ImageOutputException extends Exception
{

  public ImageOutputException()
  {
  }

  public ImageOutputException(String message)
  {
    super(message);
  }

  public ImageOutputException(Throwable cause)
  {
    super(cause);
  }

  public ImageOutputException(String message, Throwable cause)
  {
    super(message, cause);
  }

  public ImageOutputException(String message, Throwable cause,
          boolean enableSuppression, boolean writableStackTrace)
  {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
