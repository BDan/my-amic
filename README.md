# my-amic
Emulator pentru computerele aMic si PRAE.

![](https://github.com/BDan/my-amic/blob/master/my-amic/doc/img/keyboard_layout_small.gif "Tastatura aMIC")

## Descriere

Proiectul reproduce în software funcționalitatea calculatoarelor personale aMIC și PRAE, concepute și fabricate în Romania la începutul anilor 80. Descoperiți cum se lucra cu acele mașini disponibile acum doar muzeelor și colecționarilor, ce posibilități aveau și cum arătau interfețele acestora.  

## Cum a fost realizat

Descrierea hardware, în special dispunerea memoriei și maparea porturilor I/O sunt preluate după carți din epocă (precum "Totul despre calculatorul personal aMIC" - Prof. A. Petrescu și colaboratori). Structura de baza a aplicației incluzând emularea procesorului Z80, arhitectura emulatorului, emularea audio și multe altele sunt preluate din proiectul open source [QAOP](https://github.com/ukwa/qaop) initial de Jan Bobrowski. Firmware-ul, adică conținutul memoriei EPROM și softurile pre-instalate au fost oferite cu generozitate de participanți pe forumul RomanianHomeComputers, fiind preluate de pe exemplare originale, de colecție, de asemenea mașini. Varianta de monitor 0.1 a fost scanata și procesata OCR din cartea amintită. Softul VISIBLE Z-80 de M. Patrubany disponibil în emulatorul de aMIC a fost recuperat de pe caseta audio ce acompania volumul "Totul Despre Microprocesorul Z80" de același autor. 

## Cum îl rulez

1) Fiind scris în Java, programul necesită o mașină virtuala (versiune 7 sau mai nouă).Instrucțiuni de instalare găsiți aici.

2) Descărcați aplicația in format arhivat ZIP aici. Copiați fișierul-arhivă pe PC-ul propriu, apoi dezarhivați conținutul în directorul dorit.

3) În directorul respectiv rulați emulatorul aMIC cu scriptul `amic.bat` iar pentru PRAE `prae.bat`
