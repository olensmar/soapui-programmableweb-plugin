package com.smartbear.restplugin

import com.eviware.soapui.impl.WsdlInterfaceFactory
import com.eviware.soapui.impl.wsdl.WsdlProject
import com.eviware.soapui.support.UISupport
import com.eviware.soapui.support.action.support.AbstractSoapUIAction
import com.eviware.x.dialogs.Worker
import com.eviware.x.dialogs.XProgressMonitor
import com.eviware.x.form.XFormDialog
import com.eviware.x.form.support.ADialogBuilder
import groovyx.net.http.HTTPBuilder
import org.apache.http.params.HttpConnectionParams

import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import java.awt.*

class AddWsdlFromProgrammableWebAction extends AbstractSoapUIAction<WsdlProject> {

    private static final String SOAP_LISTING_URL = "http://www.programmableweb.com/apis/directory/1?protocol=SOAP"
    private static final String PROGRAMMABLEWEB_LISTING_URL = "com.smartbear.soapui.programmableweb.listingurl"
    private static final String PROGRAMMABLEWEB_DETAIL_URL = "com.smartbear.soapui.programmableweb.detailurl"

    private XFormDialog dialog = null
    private def apiEntries = [:]
    private def selectedEntries = [:]
    private JList categoryList;
    private JList apiList;

    public AddWsdlFromProgrammableWebAction() {
        super("Add API from ProgrammableWeb", "Imports a WSDL for an API in the ProgrammableWeb directory");
    }

    void perform(WsdlProject project, Object o) {
        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(AddFromProgrammableWebForm.class);
            def dlg = UISupport.dialogs.createProgressDialog("Loading API Directory...", 0, "", false)
            dlg.run(new Worker.WorkerAdapter() {
                Object construct(XProgressMonitor monitor) {
                    initEntries()
                }
            })

            def cnt = 0
            apiEntries.each { cnt += it.value.size() }
            dialog.setValue(AddFromProgrammableWebForm.STATUS, "$cnt APIs loaded")

            categoryList = new JList(apiEntries.keySet().toArray())
            apiList = new JList()

            dialog.getFormField(AddFromProgrammableWebForm.NAME).setProperty("component", new JScrollPane(apiList))
            dialog.getFormField(AddFromProgrammableWebForm.NAME).setProperty("preferredSize", new Dimension(300, 100))

            dialog.getFormField(AddFromProgrammableWebForm.CATEGORY).setProperty("component", new JScrollPane(categoryList))
            dialog.getFormField(AddFromProgrammableWebForm.CATEGORY).setProperty("preferredSize", new Dimension(300, 100))

            categoryList.addListSelectionListener { ListSelectionEvent e ->
                Object category = categoryList.selectedValue
                if (apiEntries.containsKey(category)) {
                    selectedEntries.clear()
                    apiEntries.get(category).each { selectedEntries[it.name] = it }
                    apiList.setListData(selectedEntries.keySet().toArray())
                }
            } as ListSelectionListener

            apiList.addListSelectionListener({ ListSelectionEvent e ->
                Object entry = apiList.selectedValue
                if (selectedEntries.containsKey(entry)) {
                    def apiEntry = selectedEntries[entry]

                    dialog.setValue(AddFromProgrammableWebForm.DESCRIPTION, apiEntry.description)
                    dialog.setValue(AddFromProgrammableWebForm.WSDL, getWsdlEndpoint(apiEntry.id))
                }
            } as ListSelectionListener)
        }

        dialog.setValue(AddFromProgrammableWebForm.DESCRIPTION, "")
        dialog.setValue(AddFromProgrammableWebForm.WSDL, "")
        if (dialog.show()) {
            def wsdl = dialog.getValue(AddFromProgrammableWebForm.WSDL)
            if (wsdl.trim().length() > 0)
                WsdlInterfaceFactory.importWsdl(project, wsdl, true)
            else
                UISupport.showErrorMessage("Missing WSDL to import")
        }
    }

    def initEntries() {
        def http = new HTTPBuilder(System.getProperty(PROGRAMMABLEWEB_LISTING_URL, SOAP_LISTING_URL))
        def params = http.client.params

        HttpConnectionParams.setConnectionTimeout(params, 10000)
        HttpConnectionParams.setSoTimeout(params, 10000)

        def html = http.get([:])

        html.depthFirst().findAll { it.name().toLowerCase() == "table" && it.@id == "apis" }.each
                {
                    it.depthFirst().findAll { it.name().toLowerCase() == "tr" && it.TH.size() == 0 }.each
                            {
                                def entry = [:]

                                entry.name = it.TD[0].A.text()
                                entry.id = it.TD[0].A.@href.text()
                                entry.description = it.TD[1].text()

                                def category = it.TD[2].text()

                                if (!apiEntries.containsKey(category))
                                    apiEntries[category] = []

                                apiEntries[category].add(entry)
                            }
                }

        apiEntries
    }

    String getWsdlEndpoint(String id) {
        def http = new HTTPBuilder(System.getProperty(PROGRAMMABLEWEB_DETAIL_URL, "http://www.programmableweb.com" + id))
        def html = http.get([:])
        def wsdl = ""

        html."**".find { it.name().toLowerCase() == "dt" && it.text() == "WSDL" }.each
                {
                    wsdl = it.parent().DD.A.@href.text()
                    wsdl = wsdl == null ? "" : wsdl.trim()
                }

        return wsdl
    }
}
