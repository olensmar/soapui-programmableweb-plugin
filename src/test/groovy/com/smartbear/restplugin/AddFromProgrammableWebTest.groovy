package com.smartbear.restplugin

class AddFromProgrammableWebTest extends GroovyTestCase {
    void testGetEntries() {
        def action = new AddWsdlFromProgrammableWebAction()
        def entries = action.initEntries()

        assertTrue("Checking for categories", entries.size() > 0)

        def cnt = 0
        entries.values().each { cnt += it.size() }
        assertTrue("Checking for at least 1500 APIs", cnt > 1500)
    }

    void testGetWsdlEndpoint() {
        def action = new AddWsdlFromProgrammableWebAction()
        def wsdl = action.getWsdlEndpoint("/api/4guysfromrolla.com")
        assertTrue("Checking WSDL endpoint", wsdl.length() > 0)

    }
}
