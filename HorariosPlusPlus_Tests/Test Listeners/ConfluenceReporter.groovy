import com.kms.katalon.core.annotation.AfterTestCase
import com.kms.katalon.core.context.TestCaseContext
import com.kms.katalon.core.util.KeywordUtil
import com.kms.katalon.core.configuration.RunConfiguration
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.util.regex.Pattern
import java.util.regex.Matcher

class ConfluenceReporter {

	// Las credenciales se cargan desde archivo de configuración
	static String CONFLUENCE_DOMAIN
	static String USER_EMAIL
	static String API_TOKEN
	static String PAGE_ID
	
	// Nombre del archivo de configuración
	static final String CONFIG_FILE_NAME = "confluence-config.properties"
	
	// Bloque estático para cargar configuración al iniciar
	static {
		loadConfiguration()
	}
	
	/**
	 * Carga la configuración desde el archivo de propiedades
	 */
	static void loadConfiguration() {
		println "[CONFIG] Buscando archivo de configuración: ${CONFIG_FILE_NAME}"
		
		try {
			// Obtener directorio del proyecto Katalon
			String projectDir = RunConfiguration.getProjectDir()
			println "[CONFIG] Directorio del proyecto: ${projectDir}"
			
			// Buscar el archivo en varias ubicaciones posibles
			List<String> possiblePaths = [
				"${projectDir}/${CONFIG_FILE_NAME}",
				"${projectDir}/Config/${CONFIG_FILE_NAME}",
				"${projectDir}/Test Listeners/${CONFIG_FILE_NAME}",
				"${projectDir}/Include/${CONFIG_FILE_NAME}"
			]
			
			File configFile = null
			
			for (String path : possiblePaths) {
				File f = new File(path)
				if (f.exists()) {
					configFile = f
					println "[CONFIG] Archivo encontrado en: ${path}"
					break
				}
			}
			
			if (configFile == null || !configFile.exists()) {
				println "[ERROR] No se encontró ${CONFIG_FILE_NAME} en ninguna ubicación"
				println "[ERROR] Ubicaciones buscadas:"
				possiblePaths.each { println "  - ${it}" }
				println "[ERROR] Por favor, crea el archivo ${CONFIG_FILE_NAME} en la raíz del proyecto"
				return
			}
			
			// Leer el archivo de configuración
			configFile.eachLine { line ->
				line = line.trim()
				// Ignorar comentarios y líneas vacías
				if (line && !line.startsWith("#")) {
					def parts = line.split("=", 2)
					if (parts.length == 2) {
						String key = parts[0].trim()
						String value = parts[1].trim()
						
						switch(key) {
							case "CONFLUENCE_DOMAIN":
								CONFLUENCE_DOMAIN = value
								break
							case "CONFLUENCE_USER_EMAIL":
								USER_EMAIL = value
								break
							case "CONFLUENCE_API_TOKEN":
								API_TOKEN = value
								break
							case "CONFLUENCE_PAGE_ID":
								PAGE_ID = value
								break
						}
					}
				}
			}
			
			// Validar configuración
			if (CONFLUENCE_DOMAIN && USER_EMAIL && API_TOKEN && PAGE_ID) {
				println "[CONFIG] Configuración cargada correctamente"
				println "[CONFIG] Domain: ${CONFLUENCE_DOMAIN}"
				println "[CONFIG] User: ${USER_EMAIL}"
				println "[CONFIG] Page ID: ${PAGE_ID}"
			} else {
				println "[ERROR] Configuración incompleta:"
				println "[ERROR] CONFLUENCE_DOMAIN: ${CONFLUENCE_DOMAIN ? 'OK' : 'FALTA'}"
				println "[ERROR] CONFLUENCE_USER_EMAIL: ${USER_EMAIL ? 'OK' : 'FALTA'}"
				println "[ERROR] CONFLUENCE_API_TOKEN: ${API_TOKEN ? 'OK' : 'FALTA'}"
				println "[ERROR] CONFLUENCE_PAGE_ID: ${PAGE_ID ? 'OK' : 'FALTA'}"
			}
			
		} catch (Exception e) {
			println "[ERROR] Error al cargar configuración: ${e.getMessage()}"
			e.printStackTrace()
		}
	}

	@AfterTestCase
	def reportToConfluence(TestCaseContext testCaseContext) {
		
		println "============================================"
		println "[CONFLUENCE REPORTER] Iniciando reporte..."
		println "============================================"
		
		// Verificar configuración
		if (!CONFLUENCE_DOMAIN || !USER_EMAIL || !API_TOKEN || !PAGE_ID) {
			println "[ERROR] Configuración incompleta. No se puede reportar a Confluence."
			println "[ERROR] Asegúrate de crear el archivo ${CONFIG_FILE_NAME} en la raíz del proyecto"
			KeywordUtil.markWarning("[CONFLUENCE] Configuración incompleta")
			return
		}
		
		try {
			// Obtener datos del test case
			String fullTestCaseId = testCaseContext.getTestCaseId()
			String testCaseName = fullTestCaseId.substring(fullTestCaseId.lastIndexOf('/') + 1)
			String status = testCaseContext.getTestCaseStatus()
			
			println "[INFO] Test Case: ${testCaseName}"
			println "[INFO] Status: ${status}"
			
			// Crear timestamp
			String timestamp = new Date().format("yyyy-MM-dd HH:mm:ss")
			
			// Crear el HTML del nuevo resultado
			String statusColor = (status == 'PASSED') ? '#36B37E' : '#FF5630'
			String newResultHtml = """<tr><td>${testCaseName}</td><td><strong style="color:${statusColor}">${status}</strong></td><td>${timestamp}</td></tr>"""
            
            // Paso 1: Obtener la página actual
            println "[PASO 1] Obteniendo página actual..."
            Map pageData = getPageContent()
            
            if (pageData == null) {
                println "[ERROR] No se pudo obtener la página"
                return
            }
            
            String currentContent = pageData.content
            int currentVersion = pageData.version
            String pageTitle = pageData.title
            
            println "[INFO] Página: ${pageTitle}"
            println "[INFO] Versión actual: ${currentVersion}"
            
            // DEBUG: Mostrar el contenido completo para diagnosticar
            println "========== CONTENIDO ACTUAL DE CONFLUENCE =========="
            println currentContent
            println "===================================================="
            
            // Paso 2: Actualizar o agregar el resultado al contenido
            println "[PASO 2] Procesando resultado..."
            String updatedContent = updateOrAppendResult(currentContent, testCaseName, newResultHtml)
            
            // Paso 3: Actualizar la página
            println "[PASO 3] Actualizando página..."
            boolean success = updatePage(pageTitle, updatedContent, currentVersion + 1)
            
            if (success) {
                KeywordUtil.markPassed("[CONFLUENCE] Resultado guardado en la página exitosamente")
                println "[SUCCESS] ¡Página actualizada correctamente!"
            } else {
                KeywordUtil.markWarning("[CONFLUENCE] No se pudo actualizar la página")
            }
            
        } catch (Exception e) {
            println "[EXCEPTION] Error: ${e.getMessage()}"
            e.printStackTrace()
            KeywordUtil.markWarning("[CONFLUENCE] Error: ${e.getMessage()}")
        }
        
        println "============================================"
    }
    
    /**
	 * Obtiene el contenido actual de la página
	 */
    private Map getPageContent() {
        String endpoint = "${CONFLUENCE_DOMAIN}/wiki/rest/api/content/${PAGE_ID}?expand=body.storage,version"
        
        try {
            String authString = "${USER_EMAIL}:${API_TOKEN}".bytes.encodeBase64().toString()
            
            URL url = new URL(endpoint)
            HttpURLConnection conn = (HttpURLConnection) url.openConnection()
            conn.setRequestMethod("GET")
            conn.setRequestProperty("Authorization", "Basic ${authString}")
            conn.setRequestProperty("Accept", "application/json")
            conn.setConnectTimeout(30000)
            conn.setReadTimeout(30000)
            
            int responseCode = conn.getResponseCode()
            println "[DEBUG] GET Response Code: ${responseCode}"
            
            if (responseCode == 200) {
                String response = conn.getInputStream().getText("UTF-8")
                JsonSlurper slurper = new JsonSlurper()
                def json = slurper.parseText(response)
                
                return [
                    content: json.body.storage.value,
                    version: json.version.number,
                    title: json.title
                ]
            } else {
                String errorResponse = conn.getErrorStream()?.getText("UTF-8") ?: "Sin detalles"
                println "[ERROR] GET falló: ${responseCode} - ${errorResponse}"
                printDiagnostics(responseCode)
                return null
            }
            
        } catch (Exception e) {
            println "[ERROR] Excepción en GET: ${e.getMessage()}"
            return null
        }
    }
    
    /**
	 * Actualiza una fila existente o agrega una nueva si no existe
	 */
    private String updateOrAppendResult(String currentContent, String testCaseName, String newResultHtml) {
        
        println "[DEBUG] Buscando fila existente para: ${testCaseName}"
        
        // Buscar >NOMBRE_TEST< en el contenido
        String searchPattern = ">" + testCaseName + "<"
        println "[DEBUG] Buscando patrón: ${searchPattern}"
        
        int namePosition = currentContent.indexOf(">" + testCaseName + "<")
        
        if (namePosition != -1) {
            println "[INFO] ¡Test case encontrado en posición ${namePosition}!"
            
            // Encontrar el <tr> que contiene este test case
            String beforeName = currentContent.substring(0, namePosition)
            
            // Buscar el último <tr antes de esta posición
            int lastTrSimple = beforeName.lastIndexOf("<tr>")
            int lastTrWithAttr = beforeName.lastIndexOf("<tr ")
            int trStart = Math.max(lastTrSimple, lastTrWithAttr)
            
            println "[DEBUG] Posición de <tr>: ${trStart}"
            
            if (trStart != -1) {
                // Buscar el </tr> que cierra esta fila
                String fromTr = currentContent.substring(trStart)
                int trEndRelative = fromTr.indexOf("</tr>")
                
                if (trEndRelative != -1) {
                    int trEnd = trStart + trEndRelative + 5 // +5 para incluir "</tr>"
                    
                    String existingRow = currentContent.substring(trStart, trEnd)
                    println "[DEBUG] Fila existente encontrada (${existingRow.length()} chars)"
                    
                    // Reemplazar la fila existente
                    String updatedContent = currentContent.substring(0, trStart) + newResultHtml + currentContent.substring(trEnd)
                    println "[INFO] Fila reemplazada exitosamente"
                    return updatedContent
                }
            }
        }
        
        // No existe - agregar como nueva fila
        println "[INFO] No se encontró fila existente. Agregando nueva fila..."
        return appendResultToContent(currentContent, newResultHtml)
    }
    
    /**
	 * Agrega el nuevo resultado al contenido existente (cuando no hay fila previa)
	 */
    private String appendResultToContent(String currentContent, String newResultHtml) {
        
        // Probar múltiples patrones que Confluence podría usar
        String[] patterns = [
            "</tbody></table>",
            "</tbody> </table>",
            "</TBODY></TABLE>",
            "</tbody>\n</table>",
            "</tbody>\r\n</table>"
        ]
        
        for (String pattern : patterns) {
            int index = currentContent.lastIndexOf(pattern)
            if (index != -1) {
                println "[DEBUG] Patrón encontrado: '${pattern}' en posición ${index}"
                String beforeClose = currentContent.substring(0, index)
                String afterClose = currentContent.substring(index)
                return beforeClose + newResultHtml + afterClose
            }
        }
        
        // Si no encontró ningún patrón, buscar con regex más flexible
        println "[DEBUG] Buscando con regex..."
        def matcher = (currentContent =~ /(?i)<\/tbody>\s*<\/table>/)
        if (matcher.find()) {
            int start = matcher.start()
            println "[DEBUG] Regex encontró coincidencia en posición ${start}"
            String beforeClose = currentContent.substring(0, start)
            String afterClose = currentContent.substring(start)
            return beforeClose + newResultHtml + afterClose
        }
        
        // No hay tabla, crear una nueva
        println "[DEBUG] No se encontró tabla existente, creando nueva..."
        String tableHeader = """<h2>Resultados de Pruebas Automatizadas</h2><table><thead><tr><th>Test Case</th><th>Estado</th><th>Fecha</th></tr></thead><tbody>"""
		String tableFooter = "</tbody></table>"
		
		return currentContent + tableHeader + newResultHtml + tableFooter
	}
	
	/**
	 * Actualiza la página con el nuevo contenido
	 */
	private boolean updatePage(String title, String newContent, int newVersion) {
		String endpoint = "${CONFLUENCE_DOMAIN}/wiki/rest/api/content/${PAGE_ID}"
		
		try {
			String authString = "${USER_EMAIL}:${API_TOKEN}".bytes.encodeBase64().toString()
			
			// Construir payload
			Map payload = [
				id: PAGE_ID,
				type: "page",
				title: title,
				version: [
					number: newVersion
				],
				body: [
					storage: [
						value: newContent,
						representation: "storage"
					]
				]
			]
			
			String jsonPayload = JsonOutput.toJson(payload)
			
			println "[DEBUG] PUT Endpoint: ${endpoint}"
			println "[DEBUG] Nueva versión: ${newVersion}"
			
			URL url = new URL(endpoint)
			HttpURLConnection conn = (HttpURLConnection) url.openConnection()
			conn.setRequestMethod("PUT")
			conn.setRequestProperty("Authorization", "Basic ${authString}")
			conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
			conn.setRequestProperty("Accept", "application/json")
			conn.setConnectTimeout(30000)
			conn.setReadTimeout(30000)
			conn.setDoOutput(true)
			
			OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8")
			writer.write(jsonPayload)
			writer.flush()
			writer.close()
			
			int responseCode = conn.getResponseCode()
			println "[DEBUG] PUT Response Code: ${responseCode}"
			
			if (responseCode == 200) {
				println "[DEBUG] Página actualizada exitosamente"
				return true
			} else {
				String errorResponse = conn.getErrorStream()?.getText("UTF-8") ?: "Sin detalles"
				println "[ERROR] PUT falló: ${responseCode}"
				println "[ERROR] Respuesta: ${errorResponse}"
				printDiagnostics(responseCode)
				return false
			}
			
		} catch (Exception e) {
			println "[ERROR] Excepción en PUT: ${e.getMessage()}"
			e.printStackTrace()
			return false
		}
	}
	
	/**
	 * Imprime diagnósticos según el código de error
	 */
	private void printDiagnostics(int responseCode) {
		switch(responseCode) {
			case 401:
				println "[DIAGNÓSTICO] Error 401 - Autenticación fallida"
				break
			case 403:
				println "[DIAGNÓSTICO] Error 403 - Sin permisos"
				break
			case 404:
				println "[DIAGNÓSTICO] Error 404 - Página no encontrada"
				break
			case 409:
				println "[DIAGNÓSTICO] Error 409 - Conflicto de versión"
				break
		}
	}
}