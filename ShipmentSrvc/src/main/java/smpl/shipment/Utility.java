package smpl.shipment;

public class Utility
{
	
    public static int validateStringField(String field, String fieldName, int count, StringBuilder errors)
    {
    	int countErr=count;
        if (isNullOrEmpty(field))
        {
            if (countErr == 0)
            {
                errors.append(String.format("\"Empty %s field\"", fieldName));
            }
            else
            {
                errors.append(String.format(",\"Empty %s field\"", fieldName));
            }
            countErr += 1;
        }
        return countErr;
    }

    public static boolean isNullOrEmpty(String str)
    {
        return str == null || str.isEmpty();
    }
}