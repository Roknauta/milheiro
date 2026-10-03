package com.roknauta.milheiro.web;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.primefaces.PrimeFaces;

public abstract class ControllerBase {

    @PostConstruct
    public void postConstruct(){}

    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    protected void addErrorMessage(final String messageKey, final Object... arguments) {
        addMessage(FacesMessage.SEVERITY_ERROR, messageKey, arguments);
    }

    protected void addWarningMessage(final String messageKey, final Object... arguments) {
        addMessage(FacesMessage.SEVERITY_WARN, messageKey, arguments);
    }

    protected void addInfoMessage(final String messageKey, final Object... arguments) {
        addMessage(FacesMessage.SEVERITY_INFO, messageKey, arguments);
    }

    protected void addFatalMessage(final String messageKey, final Object... arguments) {
        addMessage(FacesMessage.SEVERITY_FATAL, messageKey, arguments);
    }

    protected void addMessage(final String clientId, final FacesMessage.Severity severity, final String messageKey,
                              final Object... arguments) {
        getFacesContext().addMessage(clientId, new FacesMessage(severity, Msg.get(messageKey, arguments), null));
    }

    protected void addMessage(final FacesMessage.Severity severity, final String messageKey,
                              final Object... arguments) {
        addMessage(null, severity, messageKey, arguments);
    }

    protected void closeDialog(final String dialogId) {
        PrimeFaces.current().executeScript("PF('" + dialogId + "').hide();");
    }
}
