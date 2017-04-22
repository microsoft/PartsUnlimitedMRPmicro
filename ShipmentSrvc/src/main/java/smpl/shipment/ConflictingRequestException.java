package smpl.shipment;

/**
 * Communicating a conflicting REST request from a called API to the controller, which should use
 * it to create a CONFLICT HTTP response.
 */
@SuppressWarnings("serial")
public class ConflictingRequestException
        extends BadRequestException
{
    public ConflictingRequestException(String message)
    {
        super(message);
    }
}
