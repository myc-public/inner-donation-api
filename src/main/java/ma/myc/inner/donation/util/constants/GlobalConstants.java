package ma.myc.inner.donation.util.constants;

public final class GlobalConstants {

	private GlobalConstants() throws InstantiationException {
		throw new InstantiationException("Instances of this type are forbidden");
	}

	/**
	 * SCOPE
	 **/
	public static final String SCOPE_INNER = "inner:donation";
	public static final String SCOPE = "SCOPE_" + SCOPE_INNER;

	/**
	 * OpenApi
	 **/
	public static final String INFO_API_TITLE = "MYC Donation API";
	public static final String INFO_API_DESCRIPTION = "Inner Donation Api description";
	public static final String INFO_API_TERMS_OF_SERVICE = "https://www.myc.ma/mentions-legales";
	public static final String CONTACT_NAME = "myc Morocco";
	public static final String CONTACT_EMAIL = "helpdesk@myc.ma";
	public static final String CONTACT_WEBSITE = "https://myc.ma";
	public static final String DONATION_APIS_TAG = "donation-apis";

	/**
	 * REQUEST HEADERS
	 **/
	public static final String HEADER_BEARER = "Bearer ";

}
