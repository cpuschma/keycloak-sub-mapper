package com.cpuschma;

import org.jboss.logging.Logger;
import org.keycloak.models.*;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;

import java.util.ArrayList;
import java.util.List;

public class SubjectOIDCTokenMapper extends AbstractOIDCProtocolMapper implements OIDCIDTokenMapper, OIDCAccessTokenMapper, UserInfoTokenMapper {
    private static final Logger logger = Logger.getLogger(SubjectOIDCTokenMapper.class);
    public static final String PROVIDER_ID = "lecos-subject-oidc-token-mapper";
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        logger.info(PROVIDER_ID + " class loaded");
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, SubjectOIDCTokenMapper.class);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    @Override
    public String getDisplayType() {
        return "Map subject to claim";
    }

    @Override
    public String getHelpText() {
        return "Map the subject (sub) to another claim";
    }


    @Override
    protected void setClaim(IDToken token,
                            ProtocolMapperModel mappingModel,
                            UserSessionModel userSession,
                            KeycloakSession keycloakSession,
                            ClientSessionContext clientSessionCtx) {
        UserModel user = userSession.getUser();
        String subjectId = user.getId();
        if (subjectId == null) {
            logSubjectIdMissingError(user, keycloakSession, clientSessionCtx);
            return;
        }

        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, subjectId);
    }

    private void logSubjectIdMissingError(UserModel user,
                                          KeycloakSession keycloakSession,
                                          ClientSessionContext clientSessionCtx) {
        RealmModel realm = keycloakSession.getContext().getRealm();
        new EventBuilder(realm, keycloakSession)
                .event(EventType.LOGIN_ERROR)
                .user(user)
                .client(clientSessionCtx.getClientSession().getClient())
                .error("subject_id_missing");
    }
}
