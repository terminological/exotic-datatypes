/**
 * 
 */
package uk.co.terminological.mappers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * @author rchallen
 *
 */
public enum DateFormatString {
	
	ISO_DATETIME_FORMAT	("yyyy-MM-dd'T'HH:mm:ss"),
	ISO_DATETIME_TIME_ZONE_FORMAT ("yyyy-MM-dd'T'HH:mm:ssZZ"),
	ISO_DATE_FORMAT ("yyyy-MM-dd"),
	ISO_DATE_TIME_ZONE_FORMAT ("yyyy-MM-ddZZ"),
	ISO_TIME_FORMAT ("'T'HH:mm:ss"),
	ISO_TIME_TIME_ZONE_FORMAT ("'T'HH:mm:ssZZ"),
	ISO_TIME_NO_T_FORMAT ("HH:mm:ss"),
	ISO_TIME_NO_T_TIME_ZONE_FORMAT ("HH:mm:ssZZ"),
	SMTP_DATETIME_FORMAT ("EEE, dd MMM yyyy HH:mm:ss Z"),
	RSS2 ("EEE, dd MMM yyyy HH:mm:ss z"),
	APACHE_LOG ("dd/MMM/yyyy:HH:mm:ss Z"),
	YYYYMM ("yyyyMM"),
	YYYYMMDD ("yyyyMMdd"),
	UK_DEFAULT ("dd/MM/yyyy"),
	DATESTAMP ("yyyyMMdd"),
	TIMESTAMP ("HHmmss"),
	HUMAN_MON_DAY_YEAR ("MMM dd, yyyy"),
	HL7_DATE ("yyyyMMddHHmmss")
	;
	
	String str;
	DateFormatString(String str) {
		this.str = str;
	}
	
	public String str() {return str;} 

	public DateFormat format() {return new SimpleDateFormat(str());}
	
	public Date parse(String s) throws ParseException { return format().parse(s);}
	public String format(Date d) { return format().format(d);}
	public String now() {return format().format(new Date());}
	
	public static Date tryAll(String s) throws ParseException {
		
		for (DateFormatString e: DateFormatString.values()) {
			try {
				return e.parse(s);
			} catch (ParseException exp) {
				
			} 
		}
		
		throw new ParseException("Does not match any formats",1);
	}
	
	public XMLGregorianCalendar parseForXml(String s) throws ParseException {
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(parse(s));
		try {
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException(e);
		}
	}
	
}
