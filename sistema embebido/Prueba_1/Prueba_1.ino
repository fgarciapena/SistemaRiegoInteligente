#include <Wire.h>
#include <RTClib.h>
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

byte rowPins[ROWS] = {9, 8, 7, 6}; 
byte colPins[COLS] = {5, 4, 3, 2}; 

Keypad customKeypad = Keypad(makeKeymap(hexaKeys), rowPins, colPins, ROWS, COLS);
byte testChar[8] = {
  0b11111,
  0b10001,
  0b10001,
  0b10001,
  0b10001,
  0b10001,  
  0b10001,
  0b11111
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
const int INFORMES = 5;
const int CONTRASENA = 6;

int posicion = 0;
int cursor = 0;
static unsigned long UltimoRefresco = 0;

/* ===== SETEO INICIAL ======= */
const int CANTIDAD_CIRCUITOS = 3;
int menuActual = MENU_PRINCIPAL;
bool programacionActivada = true;
bool diasExcepcion[CANTIDAD_CIRCUITOS][7] ;
int horasExcepcion[CANTIDAD_CIRCUITOS][4] ;
bool funcionamientoCircuito[CANTIDAD_CIRCUITOS];

const int PinSensorTemperaturaCircuito1 = 22;
const int TIEMPO_REFRESCO_SENSORES = 2000; //ms
char codigoSecreto[4] ={'9','8','7','6'};



void setup () {

  Serial.begin(9600);

  delay(3000); // wait for console opening

  if (! rtc.begin()) {
    Serial.println("Couldn't find RTC");
    while (1);
  } 
  
   lcd.createChar(0, testChar); // Sends the custom char to lcd
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

   pinMode(PinSensorTemperaturaCircuito1, INPUT);
   
}

void loop () {

  char tecla = LeerTecla();
  if(tecla)
  {
    LeerOpcionDeMenu(tecla);
  }
   
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

       
}


char LeerTecla()
{
  ChequearEjecucion();
  return  customKeypad.getKey();  
} 


void ChequearEjecucion()
{
  /* FUNCION QUE SE EJECUTA CADA X SEGUNDOS */ 
  
  if(millis() - UltimoRefresco >= TIEMPO_REFRESCO_SENSORES)
  {
    UltimoRefresco += TIEMPO_REFRESCO_SENSORES;

       //ACA HAY QUE CHEQUEAR LOS SENSORES E IMPLEMENTAR LA LOGICA DE LAS EXCEPCIONES!!!! 
       // evaluar sensor de luz
       // evaluar sensor de lluvia 
       //por cada circuito evaluar sensores de humedad
       // por cada circuito ver q no tenga ninguna excepcion seteada


       for(int i = 0 ;  i < CANTIDAD_CIRCUITOS ; i++)
       {
          if(hayExcepcionConfigurada(i))
          {
            Serial.println("EXCEPCION DETECTADA");
            Serial.print("CIRCUITO ");
            Serial.print(i);
            Serial.println("****************** ");  
          }
       }     
 
       
  }
}

bool hayExcepcionConfigurada(int numeroCircuito)
{
  DateTime now = rtc.now();
  if (horasExcepcion[numeroCircuito][0] == HORA_NO_VALIDA ||  horasExcepcion[numeroCircuito][1] == HORA_NO_VALIDA || horasExcepcion[numeroCircuito][2] == HORA_NO_VALIDA  || horasExcepcion[numeroCircuito][3] == HORA_NO_VALIDA  )
  {
    return false;  
  }
  if( ! diasExcepcion[numeroCircuito][now.dayOfTheWeek()])     
  {
    return false;
  }
  int desde = horasExcepcion[numeroCircuito][0] * 60 + horasExcepcion[numeroCircuito][1];
  int hasta = horasExcepcion[numeroCircuito][2] * 60 + horasExcepcion[numeroCircuito][3];
  int ahora = now.hour() *60 + now.minute(); 
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

void LeerOpcionDeMenu(char customKey)
{
    switch(menuActual)
    {
        case CONTRASENA:
          LeerTeclaContrasena(customKey);
          break;       
        case MENU_PRINCIPAL:
          LeerOpcionMenuPrincipal(customKey);
          break;       
        case EXCEPCIONES:
          LeerOpcionExcepciones(customKey);
          break;
   }  
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
      if(programacionActivada)
        lcd.print("B- PAUSAR PROGRAMA");
      else
        lcd.print("B- REANUDAR PROGRAMA");
      lcd.setCursor(0,2);
      lcd.print("C- EXCEPCIONES");
      lcd.setCursor(0,3);
      lcd.print("D- CONFIGURAR");
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
            IniciarPararManualCircuito(i);
          }
          MostrarMenuPrincipal();
          break;    
        case 'B':
            if(programacionActivada)
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
    if(funcionamientoCircuito[numeroCircuito-1])
    {
      lcd.print("PARAR  CIRCUITO ");
    }
    else
    {
      lcd.print("INICIAR  CIRCUITO ");
    }
    
    lcd.print(numeroCircuito);
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
            funcionamientoCircuito[numeroCircuito-1] = !funcionamientoCircuito[numeroCircuito-1];
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
      programacionActivada = false;
      lcd.clear();
      lcd.setCursor(0,0);
      lcd.print("--------------------");
      lcd.setCursor(0,1);
      lcd.print("|   PROGRAMACION   |");
      lcd.setCursor(0,2);
      lcd.print("|     PAUSADA      |");
      lcd.setCursor(0,3);
      lcd.print("--------------------");
      delay(3000);
    }
    
    
    void ReanudarProgramacion()
    {
      programacionActivada = true;
      lcd.clear();
      lcd.setCursor(0,0);
      lcd.print("--------------------");
      lcd.setCursor(0,1);
      lcd.print("|   PROGRAMACION   |");
      lcd.setCursor(0,2);
      lcd.print("|     REANUDADA    |");
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
        lcd.print("1. DESDE: ");    
        if(horasExcepcion[circuito][HORA_DESDE] < 10) lcd.print('0');
        lcd.print(horasExcepcion[circuito][HORA_DESDE]);
        lcd.print(":");
        if(horasExcepcion[circuito][MINUTO_DESDE] < 10) lcd.print('0');
        lcd.print(horasExcepcion[circuito][MINUTO_DESDE]);
    
        lcd.setCursor(1,2);
        lcd.print("2. HASTA: ");
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
              case '1':
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
                
              case '2':
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

/*========================================================================
                    FIN FUNCIONES EXTRA
========================================================================*/







