package es.osoco.bbva.ats.forms.cucumber.support

import static cucumber.api.groovy.Hooks.After
import static cucumber.api.groovy.Hooks.Before

Before() {

}

After() {
    repositoriesRollBack()
}

private static void repositoriesRollBack() {
    AutomationApi automationApi = AutomationApi.instance
    automationApi.contestRepository.store.clear()
    automationApi.applicantRepository.store.clear()
    automationApi.applicationRepository.store.clear()
}