package org.collectionspace.services.structureddate;

import java.math.BigInteger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl;
import org.collectionspace.services.common.CSWebApplicationException;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.context.RemoteServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextFactory;

@Path(StructuredDateClient.SERVICE_PATH)
@Produces({"application/xml"})
@Consumes({"application/xml"})
public class StructuredDateResource extends AbstractCollectionSpaceResourceImpl<StructureddateCommon, StructureddateCommon> {

	@Override
	public Class<?> getCommonPartClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServiceName() {
		return StructuredDateClient.SERVICE_NAME;
	}

	@Override
	protected String getVersionString() {
		// TODO Auto-generated method stub
		return null;
	}

	//
	// API Endpoints
	//

	@GET
	public StructureddateCommon get(@Context UriInfo ui) {
		StructureddateCommon result = null;

		try {
			ServiceContext<StructureddateCommon, StructureddateCommon> ctx = createServiceContext(getServiceName());
			MultivaluedMap<String,String> queryParams = ui.getQueryParameters();
			String displayDate = queryParams.getFirst(StructuredDateClient.DISPLAY_DATE_QP);
			String dateToParse = queryParams.getFirst(StructuredDateClient.DATE_TO_PARSE_QP);

			if (Tools.isEmpty(displayDate) != true) {
				// The normal form of this call: accepts the display date in the displayDate param,
				// returns a v1.0 Structured Date Common payload, which has the same structure as structured
				// date fields in records.
				StructuredDateInternal structuredDate = StructuredDateInternal.parse(displayDate);
				result = toStructureddateCommon(ctx.getTenantName(), structuredDate);
			} else if (Tools.isEmpty(dateToParse) != true) {
				// The deprecated form of this call: accepts the display date in the dateToParse param,
				// returns a v0.1 Structured Date Common payload, which resembles the StructuredDateInternal
				// object.
				StructuredDateInternal structuredDate = StructuredDateInternal.parse(dateToParse);
				result = toStructureddateCommonV01(ctx.getTenantName(), structuredDate);
			} else {
				String msg = String.format("Use the '%s' query parameter to specify a display date to parse.",
						StructuredDateClient.DISPLAY_DATE_QP);
				Response response =
						Response.status(Response.Status.BAD_REQUEST).entity(msg).type("text/plain").build();
				throw new CSWebApplicationException(response);
			}
		} catch(StructuredDateFormatException fe) {
			Response response = Response.status(Response.Status.BAD_REQUEST).entity(fe.getMessage()).type("text/plain").build();
					throw new CSWebApplicationException(response);
		} catch (Exception e) {
			throw bigReThrow(e, ServiceMessages.GET_FAILED);
		}

		return result;
	}

	private StructureddateCommon toStructureddateCommon(String tenantDomain, StructuredDateInternal structuredDate) {
		StructureddateCommon result = new StructureddateCommon();

		String displayDate = structuredDate.getDisplayDate();

		if (!Tools.isEmpty(displayDate)) {
			result.setDateDisplayDate(displayDate);
		}

		String earliestScalarValue = structuredDate.getEarliestScalarValue();

		if (!Tools.isEmpty(earliestScalarValue)) {
			result.setDateEarliestScalarValue(earliestScalarValue);
		}

		String latestScalarValue = structuredDate.getLatestScalarValue();

		if (!Tools.isEmpty(latestScalarValue)) {
			result.setDateLatestScalarValue(latestScalarValue);
		}

		result.setScalarValuesComputed(structuredDate.areScalarValuesComputed());

		Date earliestSingleDate = structuredDate.getEarliestSingleDate();

		if (earliestSingleDate != null) {
			if (earliestSingleDate.getYear() != null) {
				result.setDateEarliestSingleYear(BigInteger.valueOf(earliestSingleDate.getYear()));
			}

			if (earliestSingleDate.getMonth() != null) {
				result.setDateEarliestSingleMonth(BigInteger.valueOf(earliestSingleDate.getMonth()));
			}

			if (earliestSingleDate.getDay() != null) {
				result.setDateEarliestSingleDay(BigInteger.valueOf(earliestSingleDate.getDay()));
			}

			if (earliestSingleDate.getEra() != null) {
				result.setDateEarliestSingleEra(earliestSingleDate.getEra().toString(tenantDomain));
			}
		}

		Date latestDate = structuredDate.getLatestDate();

		if (latestDate != null) {
			if (latestDate.getYear() != null) {
				result.setDateLatestYear(BigInteger.valueOf(latestDate.getYear()));
			}

			if (latestDate.getMonth() != null) {
				result.setDateLatestMonth(BigInteger.valueOf(latestDate.getMonth()));
			}

			if (latestDate.getDay() != null) {
				result.setDateLatestDay(BigInteger.valueOf(latestDate.getDay()));
			}

			if (latestDate.getEra() != null) {
				result.setDateLatestEra(latestDate.getEra().toString(tenantDomain));
			}
		}

		return result;
	}

	@Deprecated
	private StructureddateCommon toStructureddateCommonV01(String tenantDomain, StructuredDateInternal structuredDate) {
		StructureddateCommon result = new StructureddateCommon();

		String displayDate = structuredDate.getDisplayDate();

		if (!Tools.isEmpty(displayDate)) {
			result.setDisplayDate(displayDate);
		}

		String earliestScalarValue = structuredDate.getEarliestScalarValue();

		if (!Tools.isEmpty(earliestScalarValue)) {
			result.setEarliestScalarValue(earliestScalarValue);
		}

		String latestScalarValue = structuredDate.getLatestScalarValue();

		if (!Tools.isEmpty(latestScalarValue)) {
			result.setLatestScalarValue(latestScalarValue);
		}

		result.setScalarValuesComputed(structuredDate.areScalarValuesComputed());

		Date earliestSingleDate = structuredDate.getEarliestSingleDate();

		if (earliestSingleDate != null) {
			result.setEarliestSingleDate(toDateCommon(tenantDomain, earliestSingleDate));
		}

		Date latestDate = structuredDate.getLatestDate();

		if (latestDate != null) {
			result.setLatestDate(toDateCommon(tenantDomain, latestDate));
		}

		return result;
	}

	@Deprecated
	private DateCommon toDateCommon(String tenantDomain, org.collectionspace.services.structureddate.Date date) {
		DateCommon result = null;

		if (date != null) {
			result = new DateCommon();

			if (date.getCertainty() != null) {
				result.setCertainty(date.getCertainty().toString());
			}

			if (date.getDay() != null) {
				result.setDay(BigInteger.valueOf(date.getDay()));
			}

			if (date.getEra() != null) {
				result.setEra(date.getEra().toString(tenantDomain));
			}

			if (date.getMonth() != null) {
				result.setMonth(BigInteger.valueOf(date.getMonth()));
			}

			if (date.getQualifierType() != null) {
				result.setQualifierType(date.getQualifierType().toString());
			}

			if (date.getQualifierUnit() != null) {
				result.setQualifierUnit(date.getQualifierUnit().toString());
			}

			if (date.getQualifierValue() != null) {
				result.setQualifierValue(BigInteger.valueOf(date.getQualifierValue()));
			}

			if (date.getYear() != null) {
				result.setYear(BigInteger.valueOf(date.getYear()));
			}
		}

		return result;
	}

	@Override
	public ServiceContextFactory<StructureddateCommon, StructureddateCommon> getServiceContextFactory() {
		return (ServiceContextFactory<StructureddateCommon, StructureddateCommon>) RemoteServiceContextFactory.get();
	}
}
