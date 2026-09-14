package ma.myc.inner.donation.exception;


public class DonorAlreadyExistsException extends RuntimeException {

    public DonorAlreadyExistsException(String message) {
        super(message);
    }
}