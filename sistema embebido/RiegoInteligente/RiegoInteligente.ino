/*
  COMANDOS ENTRADA: 
    CMD|STATUS#
    CMD|CONFIG#
    CMD|EXC#
    
    CMD|SET|PIN|XXXX#
    CMD|SET|LUZ|XX|LLUVIA|XX|HUMEDAD1|XX|HUMEDAD2|XX|HUMEDAD3|XX#
    CMD|SET|EXC|1|0,1,2,3,4,5,6|XX|YY|ZZ|WW#   (XX, CORRESPONDE A HORAS DESDE, YY CORRESPONDE A MINUTOS DESDE)
    
    CMD|AUTO|ON#
    CMD|AUTO|OFF#
        
    CMD|INICIAR|1#
    CMD|INICIAR|2#
    CMD|INICIAR|3#
    
    CMD|PARAR|1#
    CMD|PARAR|2#
    CMD|PARAR|3#

    CMD|HISTORIA#
    CMD|HISTORIA|X|luz|lluvia|humedad1|humedad1|humedad3|expecion1|excepcion1|excepcion3|ejecutando1|ejecutando2|ejecutando3#
        X=numero de lectura (1,2,3,4,5,6,7,8,9,10)
        lluvia, luz, humedad1, humedad2, humedad3 = numeros que representan los valores actiales
        excepcion1, excepcion2, excepcion3 = 1 o 0 (depende si esta dentro de una excepcion o no)
        ejecutando1, ejecutando2, ejecutando3 = 1 o 0 (dependiendo si se esta ejecutando o no)

*/

#include <Wire.h> //administra I2C
#include <RTClib.h> //reloj
#include<Keypad.h>   
#include <LiquidCrystal_I2C.h>

RTC_DS3231 rtc;
LiquidCrystal_I2C lcd(0x27,20,4);  //

char daysOfTheWeek[7][12] = {"DOM", "LUN", "MAR", "MIE", "JUE", "VIE", "SAB"};

const byte ROWS = 4; 
const byte COLS = 4; 

char hexaKeys[ROWS][COLS] = {
  {'1', '2', '3', 'A'},
  {'4', '5', '6', 'B'},
  {'7', '8', '9', 'C'},
  {'*', '0', '#', 'D'}
};

const int HORA_DESDE = 0;
const int MINUTO_DESDE = 1;
const int HORA_HASTA = 2;
const int MINUTO_HASTA = 3;

const int HORA_NO_VALIDA = 99;

const int MENU_PRINCIPAL = 0;
const int INICIO_FIN_MANUAL = 1;
const int PAUSAR_PROGRAMACION = 2;
const int REANUDAR_PROGRAMACION = 3;
const int EXCEPCIONES = 4;
const int CONFIGURACIONES = 5;
const int CONTRASENA = 6;
const int PANTALLA_INFO = 99;

int posicion = 0;
int cursor = 0;
static unsigned long UltimoRefresco = 0;
static unsigned long UltimaTeclaApretada = 0;
int menuActual = MENU_PRINCIPAL;

/* ENVIO BLUETOOTH */
const char SEPARADOR = '|';
const char FIN_LINEA = '#';
String mensaje_aux = "";
bool bluetoothActive = false;

typedef struct 
{
   int luz;
   int lluvia;
   int humedad1;
   int humedad2;
   int humedad3;
   bool Excepcion1;
   bool Excepcion2;
   bool Excepcion3;
   bool ejecutando1;
   bool ejecutando2;
   bool ejecutando3;
   String hora;
} DatosHistoricos;

DatosHistoricos datosHistoricos[10];
int ixDatosHistoricos = 0;
static unsigned long UltimaLecturaHistorico = 0;

/******************************************************************************************************************************************************* */
/******************************************************************************************************************************************************* */
/* ********************  SETEO INICIAL  *************************************** */
// LOS PINES DEL BLUETOOTH HAY QUE CONECTARLOS EN SERIAL 3 (TX BT EN RX ARDUINO Y RX BT EN TX ARDUINO) Y LOS PINES SON LOS QUE REFERENCIAN A SERIAL 3...
byte rowPins[ROWS] = {44, 42, 40, 38}; 
byte colPins[COLS] = {36, 34, 32, 30}; 
const int pinOutputDigitalReleeInicio = 8; //Desde este pin se van a conectar las salidas de los circuitos
const int pinAnalogicoSensorHumedadInicio = 0; //Los sensores de humedad deben estar en orden en las entradas analogicas arrancando desde este numero
const int pinAnalogicoSensorGotasDeLluvia = 6;
const int pinAnalogicoSensorLDRLuz = 8;

const int CANTIDAD_CIRCUITOS = 3; //OJO NO CAMBIAR, NO ESTA PROBADO
bool modoAutomatico = true;
bool diasExcepcion[CANTIDAD_CIRCUITOS][7] ;
int horasExcepcion[CANTIDAD_CIRCUITOS][4] ;
bool funcionamientoCircuito[CANTIDAD_CIRCUITOS];

int limiteSensorHumedad = 50; //indica el limite que define que haya o no humedad
int  limiteSensorLDR = 50; //indica nuestro limite que define la cantidad de luz
int limiteSensorLluvia = 50;

const int TIEMPO_REFRESCO_SENSORES = 3000; //ms
const int TIEMPO_SALVAR_HISTORICO = 10000; //ms
const int TIEMPO_PANTALLA_INFO = 10000; //ms
char codigoSecreto[4] ={'9','8','7','6'};


/******************************************************************************************************************************************************* */
/******************************************************************************************************************************************************* */
/******************************************************************************************************************************************************* */


Keypad customKeypad = Keypad(makeKeymap(hexaKeys), rowPins, colPins, ROWS, COLS);
byte cuadrado[8] = {  0b11111,  0b10001,  0b10001,  0b10001,  0b10001,  0b10001,    0b10001,  0b11111};
byte tilde[8] = {B00000,  B00001,  B00011,  B10110,  B11100,  B01000,  B00000,  B00000};
byte cruz[8] = { B00000,  B10001,  B11011,  B01110,  B01110,  B11011,  B10001,  B00000};

void setup () {

  Serial.begin(9600);
  Serial3.begin(9600);
  
  if (! rtc.begin()) {
    Serial.println("Couldn't find RTC");
    while (1);
  } 
  
   lcd.createChar(0, cuadrado); // Sends the custom char to lcd
   lcd.createChar(1, tilde);
   lcd.createChar(2, cruz);
   lcd.init();
   lcd.init();
   lcd.backlight();
   lcd.setCursor(0,0);
   lcd.print("*******************");
   lcd.setCursor(0,1);
   lcd.print("    BIENVENIDO     ");
   lcd.setCursor(0,2);
   lcd.print("*******************");
   delay(5000);
   
   MostrarPantallaContrasena();
   UltimoRefresco = 0;
   

   //EJECUTAR SOLO LA PRIMERA VEZ
   //rtc.adjust(DateTime(F(__DATE__), F(__TIME__)));    

   pinMode(LED_BUILTIN, OUTPUT);   
   for(int i = 0 ;  i < CANTIDAD_CIRCUITOS ; i++)
   {
      pinMode(pinOutputDigitalReleeInicio + i, OUTPUT);
   }   
}

void loop () {
    char tecla = LeerTecla();
    if(tecla)
    {
      UltimaTeclaApretada = millis();
      switch(menuActual)
      {
          case PANTALLA_INFO:
            MostrarMenuPrincipal();  
            break;
          case CONTRASENA:
            LeerTeclaContrasena(tecla);
            break;       
          case MENU_PRINCIPAL:
            LeerOpcionMenuPrincipal(tecla);
            break;       
          case EXCEPCIONES:
            LeerOpcionExcepciones(tecla);
            break;
          case CONFIGURACIONES:
            LeerOpcionConfiguraciones(tecla);
            break;
      }  
    }
    else
    {
        if( menuActual != CONTRASENA &&  menuActual != PANTALLA_INFO && millis() - UltimaTeclaApretada >= TIEMPO_PANTALLA_INFO)
        {
            menuActual = PANTALLA_INFO;
            UltimaTeclaApretada = millis();    
            if( ! modoAutomatico)
            {
              lcd.clear();
              lcd.setCursor(0,0); 
              lcd.print("********************");
              lcd.setCursor(0,1); 
              lcd.print("*** MODO  MANUAL ***");
              lcd.setCursor(0,2); 
              lcd.print("***   ACTIVADO   ***");
              lcd.setCursor(0,3); 
              lcd.print("********************");               
            }
        }  
    }
}
 
char LeerTecla()
{
  char tecla = customKeypad.getKey();
  VerificarBluetooth();  
  ChequearEjecucion();
  return tecla;
} 

void VerificarBluetooth()
{
  if(Serial3.available())
  {
    bluetoothActive =  true;    
    String mensaje = extraer_mensaje();
    Serial.println("antes mensaje");
    if(mensaje != "")
    {
      Serial.println(getValor(mensaje, SEPARADOR, 0));
      if(getValor(mensaje, SEPARADOR, 0) == "CMD")
      {
        Serial.println("LEYO CMD");
        String comando = getValor(mensaje,SEPARADOR,1);
        Serial.println("VALOR");
        Serial.println(comando);
        if(comando == "STATUS"){
          SendStatusInfo();
        } if(comando == "HISTORIA"){
          SendHistoricos();
        } else if(comando == "CONFIG"){
          SendConfigInfo();
        }else if (comando = "EXC"){
          SendExcepcionInfo();
        }else if (comando = "SET"){
          ProcesarSetCommand(mensaje);
        }else if (comando = "AUTO"){
          ProcesarAutoCommand(mensaje);
        }else if (comando = "INICIAR"){
          ProcesarIniciarCommand(mensaje);
        }else if (comando = "PARAR"){
          ProcesarPararCommand(mensaje);
        }else{
          Serial.println("***COMANDO DESCONOCIDO***");
        }
      }
    }
    bluetoothActive =  false;  
  }
}

void ProcesarPararCommand(String mensaje){
    if(!modoAutomatico){
      String circuito = getValor(mensaje, SEPARADOR, 2);
      int numeroCircuito = circuito.toInt() - 1;
      if(numeroCircuito < CANTIDAD_CIRCUITOS ){
        funcionamientoCircuito[numeroCircuito] = false;
        Serial.print("PARAR CIRCUITO ");
        Serial.println(circuito);
      }
    }
    else{
        Serial.println("*** NO SE PUEDE EJECUTAR EL PARAMETRO INICUAR PORQUE EL MODO AUTOMATICO ESTA ACTIVADO");
    }
}

void ProcesarIniciarCommand(String mensaje){
    if(!modoAutomatico){
      String circuito = getValor(mensaje, SEPARADOR, 2);
      int numeroCircuito = circuito.toInt() - 1;
      if(numeroCircuito < CANTIDAD_CIRCUITOS ){
        funcionamientoCircuito[numeroCircuito] = true;
        Serial.print("INICIAR CIRCUITO ");
        Serial.println(circuito);
        }
    }
    else{
        Serial.println("*** NO SE PUEDE EJECUTAR EL PARAMETRO PARAR PORQUE EL MODO AUTOMATICO ESTA ACTIVADO");
    }
}

void ProcesarAutoCommand(String mensaje){
    String accion = getValor(mensaje, SEPARADOR, 2);
    if(accion == "ON"){
      modoAutomatico =  true;
    }else if(accion == "OFF"){
      modoAutomatico =  false;
    }
    else{
      Serial.print("PARAMETRO AUTO DESCONOCIDO: ");
      Serial.println(accion);
    }
}

void ProcesarSetCommand(String mensaje){
  String accion = getValor(mensaje, SEPARADOR, 2);
  if(accion == "PIN"){    
    String pin = getValor(mensaje, SEPARADOR, 3);
    char copy[4];
    pin.toCharArray(copy, 4);
    for(int i = 0 ; i < 4 ; i++)
    {
      codigoSecreto[i] = copy[i];
    }
    Serial.print("Nueva Pass: ");
    Serial.println(codigoSecreto);
  
  }else if(accion == "LIMITE"){    
    String valorLuz = getValor(mensaje, SEPARADOR, 3);
    String valorLluvia = getValor(mensaje, SEPARADOR, 5);
    String valorHumedad1 = getValor(mensaje, SEPARADOR, 7);
    String valorHumedad2 = getValor(mensaje, SEPARADOR, 9);
    String valorHumedad3 = getValor(mensaje, SEPARADOR, 11);

    limiteSensorHumedad = valorHumedad1.toInt();
    limiteSensorLDR = valorLuz.toInt();
    limiteSensorLluvia = valorLluvia.toInt();
    Serial.println("Parametros modificados: ");
    Serial.print("LUZ: ");
    Serial.println(valorLuz);
    Serial.print("LLUVIA: ");
    Serial.println(valorLluvia);
    Serial.print("HUMEDAD: ");
    Serial.println(valorHumedad1);    
  }else if(accion == "EXC"){
    String strCircuito = getValor(mensaje, SEPARADOR, 3);
    int numeroCircuito = strCircuito.toInt() - 1;
    String dias = getValor(mensaje, SEPARADOR, 4);
    for(int y = 0 ; y > 7; y++)
    {
      if(dias.indexOf(String(y)) > 0){
        diasExcepcion[numeroCircuito][y]= true;
      }      
      else{
        diasExcepcion[numeroCircuito][y]= false;
      }
    }
    String hora_desde = getValor(mensaje, SEPARADOR, 5);
    String minuto_desde = getValor(mensaje, SEPARADOR, 6);
    String hora_hasta = getValor(mensaje, SEPARADOR, 7);
    String minuto_hasta = getValor(mensaje, SEPARADOR, 8);
    horasExcepcion[numeroCircuito][HORA_DESDE] = hora_desde.toInt();
    horasExcepcion[numeroCircuito][MINUTO_DESDE] = minuto_desde.toInt();
    horasExcepcion[numeroCircuito][HORA_HASTA] = hora_hasta.toInt();
    horasExcepcion[numeroCircuito][MINUTO_HASTA] = minuto_hasta.toInt();

    Serial.println("Parametros modificados: ");
    Serial.print("CIRCUITO: ");
    Serial.println(numeroCircuito);
    Serial.print("DIAS: ");
    Serial.println(dias);
    Serial.println("EXCEPCIONES: ");
    Serial.print("hora desde: ");
    Serial.println(hora_desde); 
    Serial.print("minutos desde: ");
    Serial.println(minuto_desde); 
    Serial.print("hora hasta: ");
    Serial.println(hora_hasta); 
    Serial.print("minutos hasta: ");
    Serial.println(minuto_hasta);    
  }
  else {
    Serial.println("ACCION DESCONOCIDA");
  }    
}

void SendHistoricos(){
    
    int indice = 1;
    for(int i = (ixDatosHistoricos-1); i >= 0; i--)
    {
      EnvioHistorico(datosHistoricos[i], indice);
      indice++;
    }
    for(int y = 9; y >= ixDatosHistoricos; y--)
    {
      EnvioHistorico(datosHistoricos[y], indice);
      indice++;
    }
}
void EnvioHistorico(DatosHistoricos dato, int indice)
{
      String mensaje = "";
      mensaje.concat("CMD");  
      mensaje.concat(SEPARADOR);  
      mensaje.concat("HISTORIA");  
      mensaje.concat(SEPARADOR);  
      mensaje.concat(indice);
      mensaje.concat(SEPARADOR);  
      mensaje.concat(dato.luz);
      mensaje.concat(SEPARADOR);  
      mensaje.concat(dato.lluvia);
      mensaje.concat(SEPARADOR);  
      mensaje.concat(dato.humedad1);
      mensaje.concat(SEPARADOR);  
      mensaje.concat(dato.humedad2);
      mensaje.concat(SEPARADOR);  
      mensaje.concat(dato.humedad3);
      mensaje.concat(SEPARADOR);  
      if(dato.Excepcion1)
        mensaje.concat("SI");  
      else
        mensaje.concat("NO"); 
        
      mensaje.concat(SEPARADOR);  
      
      if(dato.Excepcion2)
        mensaje.concat("SI");  
      else
        mensaje.concat("NO"); 
        
      mensaje.concat(SEPARADOR);  
      
      if(dato.Excepcion3)
        mensaje.concat("SI");  
      else
        mensaje.concat("NO"); 
        
      mensaje.concat(SEPARADOR);  
       if(dato.ejecutando1)
        mensaje.concat("SI");  
      else
        mensaje.concat("NO"); 
        
      mensaje.concat(SEPARADOR);  
       if(dato.ejecutando2)
        mensaje.concat("SI");  
      else
        mensaje.concat("NO");         
      mensaje.concat(SEPARADOR);  
      
      if(dato.ejecutando3)
        mensaje.concat("SI");  
      else
        mensaje.concat("NO"); 
      mensaje.concat(SEPARADOR);  
      
      mensaje.concat(dato.hora);
      mensaje.concat(FIN_LINEA);
            
      writeString(mensaje);  
          
      Serial.print("ENVIO: ");
      Serial.println(mensaje);
    
      delay(100);
}

void SendStatusInfo(){
   int lluvia = map(analogRead(pinAnalogicoSensorGotasDeLluvia),1023,0,0,100); //mapea los valores en un rango de 0 a 100 (porcentaje)      
   int luz = map(analogRead(pinAnalogicoSensorLDRLuz),0,1023,0,100); //mapea los valores en un rango de 0 a 100 (porcentaje)      

    String mensaje = "";
    mensaje.concat("CMD");  
    mensaje.concat(SEPARADOR);  
    mensaje.concat("STATUS");  
    mensaje.concat(SEPARADOR);  
    mensaje.concat(luz);  
    mensaje.concat(SEPARADOR);  
    mensaje.concat(lluvia);  
    mensaje.concat(SEPARADOR);  
    if(modoAutomatico)
      mensaje.concat("SI");  
    else
      mensaje.concat("NO");  
    mensaje.concat(FIN_LINEA);  
    
    writeString(mensaje);  
    
    Serial.print("ENVIO: ");
    Serial.println(mensaje);
    
    delay(100);
    for(int i = 0 ;  i < CANTIDAD_CIRCUITOS ; i++)
    {  
      mensaje = "";
      mensaje.concat("CMD");  
      mensaje.concat(SEPARADOR);  
      mensaje.concat(i+1);  
      mensaje.concat(SEPARADOR);  
      mensaje.concat(map(analogRead(pinAnalogicoSensorHumedadInicio + i),1023,0,0,100));  
      mensaje.concat(SEPARADOR);  
       if(funcionamientoCircuito[i])
        mensaje.concat("SI");  
      else
        mensaje.concat("NO"); 
      mensaje.concat(SEPARADOR);  
       String dias = "";
      for(int y = 0 ; y < 7; y++)
      {
        if(diasExcepcion[i][y]){
          dias.concat(y);
          dias.concat(",");
        }      
      }
      mensaje.concat(dias); 
      mensaje.concat(SEPARADOR); 
      mensaje.concat(horasExcepcion[i][HORA_DESDE]);  
      mensaje.concat(SEPARADOR);  
      mensaje.concat(horasExcepcion[i][MINUTO_DESDE]);  
      mensaje.concat(SEPARADOR); 
      mensaje.concat(horasExcepcion[i][HORA_HASTA]);  
      mensaje.concat(SEPARADOR); 
      mensaje.concat(horasExcepcion[i][MINUTO_HASTA]);  
      mensaje.concat(FIN_LINEA);  
      
      writeString(mensaje);
    
      Serial.print("ENVIO: ");
      Serial.println(mensaje);
      
      delay(500);    
    }    
}


void SendConfigInfo(){
  String mensaje = "";
  mensaje.concat("CMD");  
  mensaje.concat(SEPARADOR);  
  mensaje.concat("CONFIG");  
  mensaje.concat(SEPARADOR);  
  mensaje.concat(limiteSensorLDR);  
  mensaje.concat(SEPARADOR);  
  mensaje.concat(limiteSensorLluvia);  
  mensaje.concat(SEPARADOR);  
  mensaje.concat(codigoSecreto);  
  mensaje.concat(FIN_LINEA);  

   writeString(mensaje);

  Serial.print("ENVIO: ");
  Serial.println(mensaje);

  delay(500);  

  for(int i = 0 ;  i < CANTIDAD_CIRCUITOS ; i++)
  { 
    mensaje = "";
    mensaje.concat("CMD");  
    mensaje.concat(SEPARADOR);  
    mensaje.concat(i+1);  
    mensaje.concat(SEPARADOR);  
    mensaje.concat(limiteSensorHumedad);  
    mensaje.concat(SEPARADOR);  
     String dias = "";
    for(int y = 0 ; y < 7; y++)
    {
      if(diasExcepcion[i][y]){
        dias.concat(y);
        dias.concat(",");
      }      
    }
    mensaje.concat(dias); 
    mensaje.concat(SEPARADOR); 
    mensaje.concat(horasExcepcion[i][HORA_DESDE]);  
    mensaje.concat(SEPARADOR);  
    mensaje.concat(horasExcepcion[i][MINUTO_DESDE]);  
    mensaje.concat(SEPARADOR); 
    mensaje.concat(horasExcepcion[i][HORA_HASTA]);  
    mensaje.concat(SEPARADOR); 
    mensaje.concat(horasExcepcion[i][MINUTO_HASTA]);  
    mensaje.concat(FIN_LINEA);        
    
    writeString(mensaje);
  
    Serial.print("ENVIO: ");
    Serial.println(mensaje);
  
    delay(500);  
  }
}



void SendExcepcionInfo(){
  for(int i = 0 ;  i < CANTIDAD_CIRCUITOS ; i++)
  {
    String mensaje = "";
    mensaje.concat("CMD");  
    mensaje.concat(SEPARADOR);  
    mensaje.concat("EXC");  
    mensaje.concat(SEPARADOR);  
    mensaje.concat(i+1);  
    mensaje.concat(SEPARADOR);  
    String dias = "";
    for(int y = 0 ; y < 7; y++)
    {
      if(diasExcepcion[i][y]){
        dias.concat(y);
        dias.concat(",");
      }      
    }
    mensaje.concat(dias); 
    mensaje.concat(SEPARADOR);  
    mensaje.concat(horasExcepcion[i][HORA_DESDE]);  
    mensaje.concat(SEPARADOR);  
    mensaje.concat(horasExcepcion[i][MINUTO_DESDE]);  
    mensaje.concat(SEPARADOR); 
    mensaje.concat(horasExcepcion[i][HORA_HASTA]);  
    mensaje.concat(SEPARADOR); 
    mensaje.concat(horasExcepcion[i][MINUTO_HASTA]);  
    mensaje.concat(FIN_LINEA);  
  
    writeString(mensaje);
  
    Serial.print("ENVIO: ");
    Serial.println(mensaje);
  
    delay(500); 
  } 
}


String extraer_mensaje(){ //
  char character;  
  String _return = "";  
  character = Serial3.read();
  if(character != FIN_LINEA){
    mensaje_aux.concat(character);
  }
  else{
    _return = mensaje_aux;
    mensaje_aux = "";
    Serial.print("RECIBO: ");
    Serial.println(_return);
  }

  return _return;
}



void ChequearEjecucion()
{
  if(modoAutomatico)
  {
    int luz;
    int lluvia;
    int humedad[CANTIDAD_CIRCUITOS];
    bool excepcion[CANTIDAD_CIRCUITOS];
    DatosHistoricos historico;
    
    /* FUNCION QUE SE EJECUTA CADA X SEGUNDOS */   
    if(millis() - UltimoRefresco >= TIEMPO_REFRESCO_SENSORES)
    {
       UltimoRefresco += TIEMPO_REFRESCO_SENSORES;
      
      
      //TODO 1: CHEQUEAR LOS SENSORES E IMPLEMENTAR LA LOGICA DE LAS EXCEPCIONES!!!! 
      // 1.evaluar sensor de luz
      // 2.evaluar sensor de lluvia 
      // por cada circuito evaluar sensores de humedad
      // por cada circuito ver q no tenga ninguna excepcion seteada
  
        lluvia = map(analogRead(pinAnalogicoSensorGotasDeLluvia),1023,0,0,100); //mapea los valores en un rango de 0 a 100 (porcentaje)      
        luz = map(analogRead(pinAnalogicoSensorLDRLuz),0,1023,0,100); //mapea los valores en un rango de 0 a 100 (porcentaje)      

        historico.luz = luz;
        historico.lluvia = lluvia;
        
        for(int i = 0 ;  i < CANTIDAD_CIRCUITOS ; i++)
        {  
          humedad[i] = map(analogRead(pinAnalogicoSensorHumedadInicio + i),1023,0,0,100);
        }      

        historico.humedad1=humedad[0];
        historico.humedad2=humedad[1];
        historico.humedad3=humedad[2];
        
        for(int i = 0;  i < CANTIDAD_CIRCUITOS ; i++)
        {            
          if(! hayExcepcionConfigurada(i))
          {
            excepcion[i] =  false;
            if(humedad[i] < limiteSensorHumedad)
            {
              if(luz < limiteSensorLDR && lluvia < limiteSensorLluvia)    
                ActivarCircuito(i);           
              else
                DesactivarCircuito(i);           
            }       
            else
            {
              DesactivarCircuito(i);           
            }
          }
          else
          {
            excepcion[i] =  true;
            DesactivarCircuito(i);           
          }
       }   
       
       historico.Excepcion1 = excepcion[0];
       historico.Excepcion2 = excepcion[1];
       historico.Excepcion3 = excepcion[2];

       historico.ejecutando1 = funcionamientoCircuito[0];
       historico.ejecutando2 = funcionamientoCircuito[1];
       historico.ejecutando3 = funcionamientoCircuito[2];
       
       DateTime now = rtc.now();
       historico.hora.concat(now.hour());
       historico.hora.concat(':');
       historico.hora.concat(now.minute());
       historico.hora.concat(':');
       historico.hora.concat(now.second());

       ProcesarHistorico(historico);
         
       if(menuActual == PANTALLA_INFO )
        {
          lcd.clear();
          lcd.setCursor(0,0);
          lcd.print("HUM: ");
          for(int i = 0 ;  i < CANTIDAD_CIRCUITOS ; i++)
          {  
            if(humedad[i] < 10) lcd.print('0');
            lcd.print(humedad[i]);
            lcd.print("%  ");          
          }      
          
          lcd.setCursor(0,1);
          lcd.print("CIR:  ");
          for(int i = 0 ;  i < CANTIDAD_CIRCUITOS ; i++)
          {
            if(funcionamientoCircuito[i])
              lcd.print((char)1);
            else
              lcd.print((char)2);
            lcd.print("   ");
          }

           lcd.setCursor(0,2);
           lcd.print("EXC:  ");
           for(int i = 0 ;  i < CANTIDAD_CIRCUITOS ; i++)
           {
              if(excepcion[i])
                lcd.print((char)1);
              else
                lcd.print((char)2);
              lcd.print("   ");
           }

          lcd.setCursor(0,3);
          lcd.print("LUZ: ");
          lcd.print(luz);
          lcd.print("% ");
          lcd.print("LLUVIA: ");
          lcd.print(lluvia);
          lcd.print("%");

        }
      }
   }
}

void ProcesarHistorico(DatosHistoricos historico)
{
  if(millis() - UltimaLecturaHistorico  >= TIEMPO_SALVAR_HISTORICO)
  {
     UltimaLecturaHistorico  += TIEMPO_SALVAR_HISTORICO;
     datosHistoricos[ixDatosHistoricos] = historico;
     ixDatosHistoricos++;
     if(ixDatosHistoricos == 10)
      ixDatosHistoricos = 0;
  }
}


void DesactivarCircuito(int circuito)
{
  funcionamientoCircuito[circuito] = false;
  digitalWrite(pinOutputDigitalReleeInicio + circuito, LOW);
}

void ActivarCircuito(int circuito)
{
  funcionamientoCircuito[circuito] = true;
  digitalWrite(pinOutputDigitalReleeInicio + circuito, HIGH);
}

bool hayExcepcionConfigurada(int numeroCircuito)
{
  DateTime now = rtc.now();
  /*  
  Serial.print("HORA ACTUAL: ");
  Serial.print(now.hour());
  Serial.print(':');
  Serial.print(now.minute());
  Serial.print(':');
  Serial.print(now.second());
  */
  if (horasExcepcion[numeroCircuito][0] == HORA_NO_VALIDA ||  horasExcepcion[numeroCircuito][1] == HORA_NO_VALIDA || horasExcepcion[numeroCircuito][2] == HORA_NO_VALIDA  || horasExcepcion[numeroCircuito][3] == HORA_NO_VALIDA  )
  {
    return false;  
  }
  if( ! diasExcepcion[numeroCircuito][now.dayOfTheWeek()])     
  {
    return false;
  }
  int desde = horasExcepcion[numeroCircuito][HORA_DESDE] * 60 + horasExcepcion[numeroCircuito][MINUTO_DESDE];
  int hasta = horasExcepcion[numeroCircuito][HORA_HASTA] * 60 + horasExcepcion[numeroCircuito][MINUTO_HASTA];
  int ahora = now.hour() * 60 + now.minute(); 
  if( ahora >= desde && ahora <= hasta)
  {
    return true;      
  }
  return false;
}

void ImprimirDateTime(DateTime dt)
{
    Serial.println("");
    Serial.print(dt.year(), DEC);
    Serial.print('/');
    Serial.print(dt.month(), DEC);
    Serial.print('/');
    Serial.print(dt.day(), DEC);
    Serial.print(' ');
    Serial.print(dt.hour(), DEC);
    Serial.print(':');
    Serial.print(dt.minute(), DEC);
    Serial.print(':');
    Serial.print(dt.second(), DEC);
}


/*========================================================================
                   CONTRASEÑA
========================================================================*/
void MostrarPantallaContrasena()
{
    lcd.clear();
    lcd.setCursor(0,0);     // situamos el cursor el la posición 2 de la linea 0.
    lcd.print("PARA INGRESAR A LA");
    lcd.setCursor(0,1);
    lcd.print("CONFIGURACION"); 
    lcd.setCursor(0,2);
    lcd.print("INTRODUZCA LA CLAVE: "); 
    lcd.setCursor(0,3);
    lcd.print("____"); 
    lcd.setCursor(0,3);
    menuActual = CONTRASENA;
    posicion = 0;
    cursor = 0;
}

void LeerTeclaContrasena(char customKey)
{
  if (customKey != '#' && customKey != '*')
  { 
     lcd.setCursor(cursor,3);
     lcd.print(customKey); 
     cursor++;                   
     if (customKey == codigoSecreto[posicion])
     {
       posicion ++; // aumentamos posicion si es correcto el digito
     }
     if (posicion == 4)
     { 
        lcd.clear();
        lcd.setCursor(3,1);      // situamos el cursor el la pos 0 de la linea 0.
        lcd.print("Clave correcta!! ");         // escribimos en LCD
        delay(1000);
        MostrarMenuPrincipal();             
     }
     else 
     {
        if(cursor >= 4)
        {
          MostrarPantallaContrasena();
        }
      }
  }
}


/*========================================================================
 *                  FIN CONTRASEÑA
========================================================================*/

/*========================================================================
                    MENU PRINCIPAL
========================================================================*/
void MostrarMenuPrincipal(){
    lcd.clear();
    lcd.setCursor(0,0);
    lcd.print("A- INICIAR / PARAR");
    lcd.setCursor(0,1);
    if(modoAutomatico)
      lcd.print("B- MODO MANUAL");
    else
      lcd.print("B- MODO AUTOMATICO");
    lcd.setCursor(0,2);
    lcd.print("C- EXCEPCIONES");
    lcd.setCursor(0,3);
    lcd.print("D- CONFIGURACION");
    lcd.setCursor(0,4);
    menuActual = MENU_PRINCIPAL;
  }

    
void LeerOpcionMenuPrincipal(char customKey)
{
  switch (customKey){
    case 'A':
      menuActual = INICIO_FIN_MANUAL;
      for(int i = 1; i <= CANTIDAD_CIRCUITOS; i++)
      {        
        IniciarPararManualCircuito(i-1);
      }
      MostrarMenuPrincipal();
      break;    
    case 'B':
        if(modoAutomatico)
        {
          menuActual = PAUSAR_PROGRAMACION;
          PausarProgramacion();
          MostrarMenuPrincipal();
        }
        else
        {
          menuActual = REANUDAR_PROGRAMACION;
          ReanudarProgramacion();
          MostrarMenuPrincipal();
        }
        break;
    case 'C':
        menuActual = EXCEPCIONES;
        MostrarOpcionesExcepciones();
        break;  
    case 'D':
        menuActual = CONFIGURACIONES;
        MostrarOpcionesConfiguraciones();
        break;    
  }   
}
/*========================================================================
                    FIN MENU PRINCIPAL
========================================================================*/


/*========================================================================
                   INICIO MANUAL
========================================================================*/
  void IniciarPararManualCircuito(int numeroCircuito)
  {
    lcd.clear();
    delay(200);
    lcd.setCursor(0,0);
    if(funcionamientoCircuito[numeroCircuito])
    {
      lcd.print("PARAR  CIRCUITO ");
    }
    else
    {
      lcd.print("INICIAR  CIRCUITO ");
    }
    
    lcd.print(numeroCircuito+1);
    lcd.setCursor(0,1);
    lcd.print("--------------------");
    lcd.setCursor(0,2);
    lcd.print("A - SI ");
    lcd.setCursor(0,3);
    lcd.print("B - NO");
    bool salir = false;
    while(!salir)
    {
      char customKey = LeerTecla();
      if(customKey)
       {
        switch(customKey)
        {
          case 'A':
            funcionamientoCircuito[numeroCircuito] = !funcionamientoCircuito[numeroCircuito];
            if(funcionamientoCircuito[numeroCircuito])
            {
              digitalWrite(pinOutputDigitalReleeInicio + numeroCircuito, HIGH);  
            }
            else
            {
              digitalWrite(pinOutputDigitalReleeInicio + numeroCircuito, LOW);  
            }
            
            salir = true;
            break;
          case 'B':
            salir = true;
            break;
        }
      }
    }
  }
/*========================================================================
                  FIN INICIO MANUAL
========================================================================*/


/*========================================================================
                    MENU INICIAR PARAR PROGRAMACION
========================================================================*/

    void PausarProgramacion()
    {
      modoAutomatico = false;
      lcd.clear();
      lcd.setCursor(0,0);
      lcd.print("--------------------");
      lcd.setCursor(0,1);
      lcd.print("|       MODO       |");
      lcd.setCursor(0,2);
      lcd.print("| MANUAL ACTIVADO  |");      
      lcd.setCursor(0,3);
      lcd.print("--------------------");
      delay(3000);
    }
    
    
    void ReanudarProgramacion()
    {
      modoAutomatico = true;
      lcd.clear();
      lcd.setCursor(0,0);
      lcd.print("--------------------");
      lcd.setCursor(0,1);
      lcd.print("|       MODO       |");
      lcd.setCursor(0,2);
      lcd.print("|  AUTO  ACTIVADO  |");
      lcd.setCursor(0,3);
      lcd.print("--------------------");
      delay(3000);
    }
  


/*========================================================================
                    FIN MENU INICIAR PARAR PROGRAMACION
========================================================================*/


/*========================================================================
                    MENU EXCEPCIONES
========================================================================*/

    void MostrarOpcionesExcepciones()
    {
      lcd.clear();
      lcd.setCursor(0,0);
      lcd.print("A- EXCEPCION DIAS");
      lcd.setCursor(0,1);
      lcd.print("B- EXCEPCION HORAS");
      lcd.setCursor(0,2);
      lcd.print("C- VER EXCEPCIONES");
      lcd.setCursor(0,3);
      lcd.print("           * - SALIR");
    }

    
    void LeerOpcionExcepciones(char customKey)
    {
        switch(customKey)
        {
           case 'A':
              for(int i = 0; i < CANTIDAD_CIRCUITOS; i++)
              {
                 ImprimirDias(i);
              }
              
              MostrarMenuPrincipal();
              break;
           case 'B':
              for(int i = 0; i < CANTIDAD_CIRCUITOS; i++)
              {
                IniciarExcepcionHoras(i);
              }          
              MostrarMenuPrincipal();
              break;
           case 'C':
              MostrarExcepciones();
              break;
              
           case '*':
              MostrarMenuPrincipal();
              break;
        }  
    }
    void MostrarExcepciones()
    {
      lcd.clear();
      lcd.setCursor(0,0);
      lcd.print("       D L M M J V S");
      for(int circuito = 0; circuito < CANTIDAD_CIRCUITOS; circuito++)
      {
        lcd.setCursor(0,circuito + 1);
        lcd.print("CIR.");
        lcd.print(circuito);
        lcd.print("  ");
        for(int dia = 0; dia < 7; dia++)
        {
          if(diasExcepcion[circuito][dia]) 
            lcd.print((char)255);
          else
            lcd.print(" ");  
          lcd.print(" ");  
        }
      }
    
      while(LeerTecla() == NO_KEY) {}  
      
      lcd.clear();
        
      for(int circuito = 0; circuito < CANTIDAD_CIRCUITOS; circuito++)
      {
        lcd.setCursor(0,circuito);
        lcd.print("CIR.");
        lcd.print(circuito+1);
        lcd.print(": ");
        if(horasExcepcion[circuito][HORA_DESDE] == HORA_NO_VALIDA || horasExcepcion[circuito][MINUTO_DESDE] == HORA_NO_VALIDA || horasExcepcion[circuito][HORA_HASTA] == HORA_NO_VALIDA || horasExcepcion[circuito][MINUTO_HASTA] == HORA_NO_VALIDA )
        {
          lcd.print("NO VALIDO");
        }
        else
        {
          if(horasExcepcion[circuito][HORA_DESDE] < 10) lcd.print('0');
          lcd.print(horasExcepcion[circuito][HORA_DESDE]);
          lcd.print(":");        
          if(horasExcepcion[circuito][MINUTO_DESDE] < 10) lcd.print('0');
          lcd.print(horasExcepcion[circuito][MINUTO_DESDE]);
          lcd.print(" - ");        
          if(horasExcepcion[circuito][HORA_HASTA] < 10) lcd.print('0');
          lcd.print(horasExcepcion[circuito][HORA_HASTA]);
          lcd.print(":");        
          if(horasExcepcion[circuito][MINUTO_HASTA] < 10) lcd.print('0');
          lcd.print(horasExcepcion[circuito][MINUTO_HASTA]);
        }
      }
    
      while(LeerTecla() == NO_KEY) {}  
    
      MostrarMenuPrincipal();
    }    
    
    

    void ImprimirDias(int circuito)
    {
    
      lcd.clear();
      lcd.setCursor(0,0);
      lcd.print("--------------------");
      lcd.setCursor(0,1);
      lcd.print("|   EXCEPCIONES    |");
      lcd.setCursor(0,2);
      lcd.print("|    CIRCUITO "); 
      lcd.print(circuito);
      lcd.print("    |");
      lcd.setCursor(0,3);
      lcd.print("--------------------");
      delay(3000);  
      lcd.clear(); 
      
       /* 0 = DOM 
             DOM  LUN  MAR  MIE  JUE  VIE  SAB 
       dias   0    1    2    3    4    5     6
       */  
       
      int _char  = 0;
      bool redibujar = true;
      
      while(redibujar)
      {    
           /* LUNES */
          if(diasExcepcion[circuito][1])
            _char = 255;
          else
            _char = 0;
          lcd.setCursor(0,0);
          lcd.print((char)_char);
          lcd.print(" 1.LUN ");
          
            /* MARTES */
         if(diasExcepcion[circuito][2])
            _char = 255;
          else
            _char = 0;
          lcd.setCursor(0,1);
          lcd.print((char)_char);
          lcd.print(" 2.MAR ");
        
            /* MIERCOLES */
          if(diasExcepcion[circuito][3])
            _char = 255;
          else
            _char = 0;
          lcd.setCursor(0,2);
          lcd.print((char)_char);
          lcd.print(" 3.MIE ");
        
          /* JUEVES */
          if(diasExcepcion[circuito][4])
            _char = 255;
          else
            _char = 0;
          lcd.setCursor(0,3);
          lcd.print((char)_char);
          lcd.print(" 4.JUE ");
        
          /* VIERNES */
          if(diasExcepcion[circuito][5])
            _char = 255;
          else
            _char = 0;
          lcd.setCursor(10,0);
          lcd.print((char)_char);
          lcd.print(" 5.VIE ");
          
          /* SABADO */
         if(diasExcepcion[circuito][6])
            _char = 255;
          else
            _char = 0;
          lcd.setCursor(10,1);
          lcd.print((char)_char);
          lcd.print(" 6.SAB ");
        
           /* DOMINGO */
          if(diasExcepcion[circuito][0])
            _char = 255;
          else
            _char = 0;
          lcd.setCursor(10,2);
          lcd.print((char)_char);
          lcd.print(" 0.DOM ");
        
          lcd.setCursor(13,3);
          lcd.print("* SALIR");
        
          bool salir = false;
          while(!salir)
          {
            char customKey = LeerTecla();
            if(customKey)
             {
              switch(customKey)
              {
                case '*':
                  salir = true;
                  redibujar = false;
                  break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                  diasExcepcion[circuito][(int)customKey - 48] = !diasExcepcion[circuito][(int)customKey - 48];
                  salir = true;
                  redibujar = true;
                  break;
              }
            }
          }  
      }
    }
    
    
    void IniciarExcepcionHoras(int circuito)
    {
      bool redibujar = true;
      
      while(redibujar)
      {    
        lcd.clear();
        lcd.setCursor(0,0);
        lcd.print(" HORARIO CIRCUITO ");
        lcd.print(circuito);
        lcd.setCursor(1,1);
        lcd.print("A. DESDE: ");    
        if(horasExcepcion[circuito][HORA_DESDE] < 10) lcd.print('0');
        lcd.print(horasExcepcion[circuito][HORA_DESDE]);
        lcd.print(":");
        if(horasExcepcion[circuito][MINUTO_DESDE] < 10) lcd.print('0');
        lcd.print(horasExcepcion[circuito][MINUTO_DESDE]);
    
        lcd.setCursor(1,2);
        lcd.print("B. HASTA: ");
        if(horasExcepcion[circuito][HORA_HASTA] < 10) lcd.print('0');
        lcd.print(horasExcepcion[circuito][HORA_HASTA]);
        lcd.print(":");
        if(horasExcepcion[circuito][MINUTO_HASTA] < 10) lcd.print('0');
        lcd.print(horasExcepcion[circuito][MINUTO_HASTA]);
        lcd.setCursor(0,3);
        lcd.print("           * - SALIR ");
    
        bool salir = false;
        while(!salir)
        {
          char customKey = LeerTecla();
          if(customKey)
           {
            switch(customKey)
            {
              case '*':
                salir = true;
                redibujar = false;
                break;
              case 'A':
                lcd.setCursor(11,1);
                lcd.print("__:__");
                lcd.setCursor(11,1);
                horasExcepcion[circuito][HORA_DESDE]= PedirNumeroDosDigitos();
                if(horasExcepcion[circuito][HORA_DESDE]  >= 24)
                {
                   lcd.setCursor(11,1);
                   lcd.print("XX");
                   horasExcepcion[circuito][HORA_DESDE]  = HORA_NO_VALIDA; 
                }
                
                lcd.setCursor(14,1);
                horasExcepcion[circuito][MINUTO_DESDE] = PedirNumeroDosDigitos();
                if(horasExcepcion[circuito][MINUTO_DESDE] >= 60)
                {
                   lcd.setCursor(14,1);
                   lcd.print("XX");
                   horasExcepcion[circuito][MINUTO_DESDE] = HORA_NO_VALIDA; 
                }
                
                salir = true;
                break;
                
              case 'B':
                lcd.setCursor(11,2);
                lcd.print("__:__");
                lcd.setCursor(11,2);
                horasExcepcion[circuito][HORA_HASTA] = PedirNumeroDosDigitos();
                if(horasExcepcion[circuito][HORA_HASTA] >= 24)
                {
                   lcd.setCursor(11,2);
                   lcd.print("XX");
                   horasExcepcion[circuito][HORA_HASTA] = HORA_NO_VALIDA; 
                }
                
                lcd.setCursor(14,2);
                horasExcepcion[circuito][MINUTO_HASTA] = PedirNumeroDosDigitos();
                if(horasExcepcion[circuito][MINUTO_HASTA]  >= 60)
                {
                   lcd.setCursor(14,2);
                   lcd.print("XX");
                   horasExcepcion[circuito][MINUTO_HASTA]  = HORA_NO_VALIDA; 
                }
                
                salir = true;
                break;
             
            }
          }
        }    
      }     
    }

/*========================================================================
                    FIN MENU EXCEPCIONES
========================================================================*/

/*========================================================================
                    MENU CONFIGURACIONES
========================================================================*/

 void MostrarOpcionesConfiguraciones()
    {
      lcd.clear();
      lcd.setCursor(0,0);
      lcd.print("A- SENSOR HUMEDAD");
      lcd.setCursor(0,1);
      lcd.print("B- SENSOR LUZ");
      lcd.setCursor(0,2);
      lcd.print("C- SENSOR LLUVIA");
      lcd.setCursor(0,3);
      lcd.print("           * - SALIR");
    }

void LeerOpcionConfiguraciones(char customKey)
{
    switch(customKey)
    {
       case 'A':
          ConfigurarHumedad();              
          MostrarMenuPrincipal();
          break;
       case 'B':
          ConfigurarLuz();
          MostrarMenuPrincipal();
          break;
       case 'C':
          ConfigurarLluvia();
          MostrarMenuPrincipal();
          break;              
       case '*':
          MostrarMenuPrincipal();
          break;
    }  
}

void ConfigurarHumedad()
{
  bool redibujar = true;
  int numero = 0;
  
  while(redibujar)
  {    
    lcd.clear();
    lcd.setCursor(0,0);
    lcd.print("TEST:");
    lcd.setCursor(6,0);
    for(int i = 0 ;  i < CANTIDAD_CIRCUITOS ; i++)
    {  
      lcd.print(map(analogRead(pinAnalogicoSensorHumedadInicio + i),1023,0,0,100));
      lcd.print("% ");
    }
    lcd.setCursor(0,1);
    lcd.print("LIMITE ACTUAL: ");    
    lcd.print(limiteSensorHumedad);   
    lcd.print("%");
            
    lcd.setCursor(0,3);
    lcd.print("A- EDITAR   * -SALIR");
        
    bool salir = false;
    while(!salir)
    {
      char customKey = LeerTecla();
      if(customKey)
       {
        switch(customKey)
        {
          case '*':
            salir = true;
            redibujar = false;
            break;
          case 'A':
            lcd.setCursor(0,2);
            lcd.print("NUEVO VALOR: ");   
            lcd.setCursor(13,2);
            lcd.print("__ %");
            lcd.setCursor(13,2);
            limiteSensorHumedad = PedirNumeroDosDigitos();
            salir = true;
            break;             
        }
      }
    }
  }
}

void ConfigurarLuz()
{
  bool redibujar = true;
  int numero = 0;
  
  while(redibujar)
  {    
    lcd.clear();
    lcd.setCursor(0,0);
    lcd.print("LUZ: ");    
    lcd.print(map(analogRead(pinAnalogicoSensorLDRLuz),0,1023,0,100));
    lcd.print("%");    
    
    lcd.setCursor(0,1);
    lcd.print("LIMITE ACTUAL:  ");    
    lcd.print(limiteSensorLDR);   
    lcd.print("%");
    lcd.setCursor(0,3);
    
    lcd.print("A- EDITAR   * -SALIR");
        
    bool salir = false;
    while(!salir)
    {
      char customKey = LeerTecla();
      if(customKey)
       {
        switch(customKey)
        {
          case '*':
            salir = true;
            redibujar = false;
            break;
          case 'A':
            lcd.setCursor(0,2);
            lcd.print("NUEVO VALOR: ");   
            lcd.setCursor(13,2);
            lcd.print("__");
            lcd.setCursor(13,2);
            limiteSensorLDR = PedirNumeroDosDigitos();
            salir = true;
            break;             
        }
      }
    }
  }
}


void ConfigurarLluvia()
{
  bool redibujar = true;
  int numero = 0;
  
  while(redibujar)
  {    
    lcd.clear();
    lcd.setCursor(0,0);
    lcd.print("LLUVIA: ");    
    lcd.print(map(analogRead(pinAnalogicoSensorGotasDeLluvia),1023,0,0,100));
    lcd.print("%");    
    
    lcd.setCursor(0,1);
    lcd.print("LIMITE ACTUAL:  ");    
    lcd.print(limiteSensorLluvia);   
    lcd.print("%");
    lcd.setCursor(0,3);
    
    lcd.print("A- EDITAR   * -SALIR");
        
    bool salir = false;
    while(!salir)
    {
      char customKey = LeerTecla();
      if(customKey)
       {
        switch(customKey)
        {
          case '*':
            salir = true;
            redibujar = false;
            break;
          case 'A':
            lcd.setCursor(0,2);
            lcd.print("NUEVO VALOR: ");   
            lcd.setCursor(13,2);
            lcd.print("__");
            lcd.setCursor(13,2);
            limiteSensorLluvia = PedirNumeroDosDigitos();
            salir = true;
            break;             
        }
      }
    }
  }
}

/*========================================================================
                    MENU CONFIGURACIONES
========================================================================*/

/*========================================================================
                    FUNCIONES EXTRA
========================================================================*/
int PedirNumeroDosDigitos()
{
   int num = 0;
   int cantidadDigitos=0;
   char key =  LeerTecla();
   while(key != '*' && cantidadDigitos < 2)
   {
      switch (key)
      {
         case NO_KEY:
            break;

         case '0': case '1': case '2': case '3': case '4':
         case '5': case '6': case '7': case '8': case '9':
            lcd.print(key);
            cantidadDigitos++;
            num = num * 10 + (key - '0');
            break;
      }
      key = LeerTecla();
   }  
   return num;
}


String getValor(String data, char separator, int index)
{
    int found = 0;
    int strIndex[] = { 0, -1 };
    int maxIndex = data.length() - 1;

    for (int i = 0; i <= maxIndex && found <= index; i++) {
        if (data.charAt(i) == separator || i == maxIndex) {
            found++;
            strIndex[0] = strIndex[1] + 1;
            strIndex[1] = (i == maxIndex) ? i+1 : i;
        }
    }
    String _return = "";
    if (found > index){
      _return = data.substring(strIndex[0], strIndex[1]); 
      _return.replace("#","");     
    }    
    
    return _return ;
}

void writeString(String stringData) { // Used to serially push out a String with Serial.write()
  for (int i = 0; i < stringData.length(); i++)  {
    Serial3.write(stringData[i]);   
  }
}

/*========================================================================
                    FIN FUNCIONES EXTRA
========================================================================*/

 /*
  
    DateTime now = rtc.now();

     lcd.print(daysOfTheWeek[now.dayOfTheWeek()]);

    lcd.clear();

    daysOfTheWeek[now.dayOfTheWeek()]
    lcd.print(now.year(), DEC);
    lcd.print('/');
    lcd.print(now.month(), DEC);
    lcd.print('/');
    lcd.print(now.day(), DEC);
    lcd.print(' ');
    lcd.print(now.hour(), DEC);
    lcd.print(':');
    lcd.print(now.minute(), DEC);
    lcd.print(':');
    lcd.print(now.second(), DEC);

    */





