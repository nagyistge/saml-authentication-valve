package org.jahia.modules.saml2.actions;

import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.modules.saml2.SAML2Util;
import org.jahia.modules.saml2.admin.SAML2Settings;
import org.jahia.modules.saml2.admin.SAML2SettingsService;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.saml.client.SAML2ClientConfiguration;
import org.pac4j.saml.crypto.KeyStoreCredentialProvider;
import org.pac4j.saml.metadata.SAML2MetadataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by smomin on 5/27/16.
 */
public class MetadataAction extends Action {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataAction.class);
    private SAML2SettingsService saml2SettingsService;

    /**
     *
     * @param req
     * @param renderContext
     * @param resource
     * @param session
     * @param parameters
     * @param urlResolver
     * @return
     * @throws Exception
     */
    @Override
    public ActionResult doExecute(final HttpServletRequest req,
                                  final RenderContext renderContext,
                                  final Resource resource,
                                  final JCRSessionWrapper session,
                                  final Map<String, List<String>> parameters,
                                  final URLResolver urlResolver) throws Exception {
        if (renderContext.getSite() == null) {
            return ActionResult.OK;
        }
        SAML2Util.initialize(() -> {
            final String siteKey = renderContext.getSite().getSiteKey();
            final SAML2Settings saml2Settings = saml2SettingsService.getSettings(siteKey);

            final SAML2ClientConfiguration saml2ClientConfiguration = new SAML2ClientConfiguration();
            saml2ClientConfiguration.setIdentityProviderMetadataPath(saml2Settings.getIdentityProviderUrl());
            saml2ClientConfiguration.setServiceProviderEntityId(saml2Settings.getRelyingPartyIdentifier());
            saml2ClientConfiguration.setKeystoreResource(CommonHelper.getResource(saml2Settings.getKeyStoreLocation()));
            saml2ClientConfiguration.setKeystorePassword(saml2Settings.getKeyStorePass());
            saml2ClientConfiguration.setPrivateKeyPassword(saml2Settings.getPrivateKeyPass());

            final KeyStoreCredentialProvider keyStoreCredentialProvider = new KeyStoreCredentialProvider(saml2ClientConfiguration);
            final SAML2MetadataGenerator saml2MetadataGenerator = new SAML2MetadataGenerator();
            saml2MetadataGenerator.setEntityId(saml2Settings.getRelyingPartyIdentifier());
            saml2MetadataGenerator.setAssertionConsumerServiceUrl(SAML2Util.getAssertionConsumerServiceUrl(req, saml2Settings.getIncomingTargetUrl()));
            saml2MetadataGenerator.setCredentialProvider(keyStoreCredentialProvider);

            renderContext.getResponse().getWriter().append(saml2MetadataGenerator.getMetadata());
        });
        return ActionResult.OK;
    }

    public void setSaml2SettingsService(SAML2SettingsService saml2SettingsService) {
        this.saml2SettingsService = saml2SettingsService;
    }
}
