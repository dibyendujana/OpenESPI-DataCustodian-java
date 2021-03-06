/*
 *     Copyright (c) 2018-2019 Green Button Alliance, Inc.
 *
 *     Portions copyright (c) 2013-2018 EnergyOS.org
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package org.greenbuttonalliance.espi.datacustodian.web.api;

import com.sun.syndication.io.FeedException;
import org.greenbuttonalliance.espi.common.domain.ApplicationInformation;
import org.greenbuttonalliance.espi.common.domain.Routes;
import org.greenbuttonalliance.espi.common.service.ApplicationInformationService;
import org.greenbuttonalliance.espi.common.service.ExportService;
import org.greenbuttonalliance.espi.common.service.ResourceService;
import org.greenbuttonalliance.espi.common.utils.ExportFilter;
import org.greenbuttonalliance.espi.datacustodian.utils.VerifyURLParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Controller
public class ApplicationInformationRESTController {

	@Autowired
	private ApplicationInformationService applicationInformationService;

	@Autowired
	private ExportService exportService;

	@Autowired
	private ResourceService resourceService;

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public void handleGenericException() {
	}

	// ROOT and XPath are the same for this one.
	//
	@RequestMapping(value = Routes.ROOT_APPLICATION_INFORMATION_COLLECTION, method = RequestMethod.GET, produces = "application/atom+xml")
	@ResponseBody
	public void index(HttpServletResponse response,
			@RequestParam Map<String, String> params) throws IOException,
			FeedException {

		// Verify request contains valid query parameters
		if(!VerifyURLParams.verifyEntries(Routes.ROOT_APPLICATION_INFORMATION_COLLECTION, params)) {

			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request contains invalid query parameter values!");
			return;
		}

		response.setContentType(MediaType.APPLICATION_ATOM_XML_VALUE);
		exportService.exportApplicationInformations(response.getOutputStream(),
				new ExportFilter(params));
	}

	//
	//
	@RequestMapping(value = Routes.ROOT_APPLICATION_INFORMATION_MEMBER, method = RequestMethod.GET, produces = "application/atom+xml")
	@ResponseBody
	public void show(HttpServletResponse response,
			@PathVariable Long applicationInformationId,
			@RequestParam Map<String, String> params) throws IOException,
			FeedException {

		// Verify request contains valid query parameters
		if(!VerifyURLParams.verifyEntries(Routes.ROOT_APPLICATION_INFORMATION_MEMBER, params)) {

			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request contains invalid query parameter values!");
			return;
		}

		response.setContentType(MediaType.APPLICATION_ATOM_XML_VALUE);
		try {
			exportService.exportApplicationInformation(
					applicationInformationId, response.getOutputStream(),
					new ExportFilter(params));
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}

	}

	//
	//
	@RequestMapping(value = Routes.ROOT_APPLICATION_INFORMATION_COLLECTION, method = RequestMethod.POST, consumes = "application/atom+xml", produces = "application/atom+xml")
	@ResponseBody
	public void create(HttpServletResponse response,
			@RequestParam Map<String, String> params, InputStream stream)
			throws IOException {

		response.setContentType(MediaType.APPLICATION_ATOM_XML_VALUE);
		try {
			ApplicationInformation applicationInformation = this.applicationInformationService
					.importResource(stream);
			exportService.exportApplicationInformation(
					applicationInformation.getId(), response.getOutputStream(),
					new ExportFilter(params));
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	//

	@RequestMapping(value = Routes.ROOT_APPLICATION_INFORMATION_MEMBER, method = RequestMethod.PUT, consumes = "application/atom+xml", produces = "application/atom+xml")
	@ResponseBody
	public void update(HttpServletResponse response,
			@PathVariable Long applicationInformationId,
			@RequestParam Map<String, String> params, InputStream stream)
			throws IOException, FeedException {
		ApplicationInformation applicationInformation = resourceService
				.findById(applicationInformationId,
						ApplicationInformation.class);

		if (applicationInformation != null) {
			try {

				ApplicationInformation newApplicationInformation = applicationInformationService
						.importResource(stream);
				applicationInformation.merge(newApplicationInformation);
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
		}
	}

	@RequestMapping(value = Routes.ROOT_APPLICATION_INFORMATION_MEMBER, method = RequestMethod.DELETE)
	public void delete(HttpServletResponse response,
			@PathVariable Long applicationInformationId,
			@RequestParam Map<String, String> params, InputStream stream)
			throws IOException, FeedException {
		try {
			resourceService.deleteById(applicationInformationId,
					ApplicationInformation.class);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	public void setApplicationInformationService(
			ApplicationInformationService applicationInformationService) {
		this.applicationInformationService = applicationInformationService;
	}

	public ApplicationInformationService getApplicationInformationService() {
		return this.applicationInformationService;
	}

	public void setExportService(ExportService exportService) {
		this.exportService = exportService;
	}

	public ExportService getExportService() {
		return this.exportService;
	}

	public void setResourceService(ResourceService resourceService) {
		this.resourceService = resourceService;
	}

	public ResourceService getResourceService() {
		return this.resourceService;
	}

}
