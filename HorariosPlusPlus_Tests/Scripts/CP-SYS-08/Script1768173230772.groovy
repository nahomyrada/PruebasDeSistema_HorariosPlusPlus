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

// 1. Abrir Navegador
WebUI.openBrowser('')
WebUI.navigateToUrl('https://horarios-plus-plus-sqa-front.onrender.com/')

// 2. Ir a la pantalla de Crear Cuenta
WebUI.click(findTestObject('Object Repository/CP-SYS-08/Page_HorariosPlusPlus/btn_CrearCuenta'))

// 3. Llenar Email usando la VARIABLE (var_user)
WebUI.setText(findTestObject('Object Repository/CP-SYS-08/Page_HorariosPlusPlus/input_text'), var_user)

// 4. Llenar Contraseña usando la VARIABLE (var_pass)
// OJO: En tu script original usabas 'input_text' también para la contraseña. 
// Si ves que escribe el password encima del correo, cambia este objeto por el que corresponda a la contraseña (ej. input_text_1).
// Por ahora lo dejo tal cual me lo pasaste:
WebUI.setText(findTestObject('Object Repository/CP-SYS-08/Page_HorariosPlusPlus/input_text'), var_pass)

// 5. Clic en Confirmar
WebUI.click(findTestObject('Object Repository/CP-SYS-08/Page_HorariosPlusPlus/btnConfirmarCuenta'))

// 6. LÓGICA INTELIGENTE (IF/ELSE)
// Aquí Katalon decide qué verificar según lo que diga tu Excel en la columna 'var_resultado'

if (var_resultado == 'Exito') {
    // CASO BUENO: Se creó la cuenta y nos redirigió al Login
    // Verificamos que aparezca el botón de Iniciar Sesión
    WebUI.verifyElementPresent(findTestObject('Object Repository/CP-SYS-08/Page_HorariosPlusPlus/btnIniciarSesion'), 10)
    
} else {
    // CASO MALO: Hubo un error (ej. contraseña débil)
    // Verificamos que aparezca el texto de contraseña inválida
    WebUI.verifyElementPresent(findTestObject('Object Repository/CP-SYS-08/Page_HorariosPlusPlus/textoDivContrasenaInvalida'), 10)
}

// 7. Cerrar Navegador
WebUI.closeBrowser()