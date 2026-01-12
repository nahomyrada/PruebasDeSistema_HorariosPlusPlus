import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import static com.kms.katalon.core.testobject.ObjectRepository.findWindowsObject
import com.kms.katalon.core.checkpoint.Checkpoint as Checkpoint
import com.kms.katalon.core.cucumber.keyword.CucumberBuiltinKeywords as CucumberKW
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.testcase.TestCase as TestCase
import com.kms.katalon.core.testdata.TestData as TestData
import com.kms.katalon.core.testng.keyword.TestNGBuiltinKeywords as TestNGKW
import com.kms.katalon.core.testobject.TestObject as TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.windows.keyword.WindowsBuiltinKeywords as Windows
import internal.GlobalVariable as GlobalVariable
import org.openqa.selenium.Keys as Keys

WebUI.openBrowser('')

WebUI.navigateToUrl('https://horarios-plus-plus-sqa-front.onrender.com/')

WebUI.click(findTestObject('Object Repository/CP-SYS-03/Page_HorariosPlusPlus/a'))

WebUI.click(findTestObject('Object Repository/CP-SYS-03/Page_HorariosPlusPlus/input_text'))

WebUI.setText(findTestObject('Object Repository/CP-SYS-03/Page_HorariosPlusPlus/input_text'), 'Admin@gmail.com')

WebUI.click(findTestObject('Object Repository/CP-SYS-03/Page_HorariosPlusPlus/input_text'))

WebUI.setText(findTestObject('Object Repository/CP-SYS-03/Page_HorariosPlusPlus/input_text'), 'Admin')

WebUI.click(findTestObject('Object Repository/CP-SYS-03/Page_HorariosPlusPlus/button_button'))

WebUI.click(findTestObject('Object Repository/CP-SYS-03/Page_HorariosPlusPlus/a_1'))

WebUI.click(findTestObject('Object Repository/CP-SYS-03/Page_HorariosPlusPlus/button_button_1'))

WebUI.click(findTestObject('Object Repository/CP-SYS-03/Page_HorariosPlusPlus/input_text_3'))

WebUI.setText(findTestObject('Object Repository/CP-SYS-03/Page_HorariosPlusPlus/input_text_3'), ' Laura')

WebUI.click(findTestObject('Object Repository/CP-SYS-03/Page_HorariosPlusPlus/button_button_2'))

WebUI.selectOptionByValue(findTestObject('Object Repository/CP-SYS-03/Page_HorariosPlusPlus/select'), '8', true)

WebUI.click(findTestObject('Object Repository/CP-SYS-03/Page_HorariosPlusPlus/button_button_3'))

WebUI.verifyElementPresent(findTestObject('Object Repository/CP-SYS-03/Page_HorariosPlusPlus/div_Administrar Usuarios_go3958317564'), 
    0)

WebUI.click(findTestObject('Object Repository/CP-SYS-03/Page_HorariosPlusPlus/button_button_4'))

WebUI.click(findTestObject('Object Repository/CP-SYS-03/Page_HorariosPlusPlus/div_Administrar Usuarios_go3958317564_1'))

WebUI.closeBrowser()

