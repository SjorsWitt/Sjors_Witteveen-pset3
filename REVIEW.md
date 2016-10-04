Names
Af en toe inconsistent met de wijze waarop variabelen benoemd worden: de ene keer met underscores en andere keer met hoofdletters.
movieList <-> movie_list
Het is beter om alleen de movieList manier te gebruiken voor variabelen.

Headers
Bij geen enkele Java class is een header aanwezig.
Kort en bondig uitleggen waar de class voor bedoeld is.

Comments
Vaak wordt alleen omschreven wanneer een bepaald stuk code aangeroepen wordt.
Bijvoorbeeld: //when search button is clicked
Beter is om ook uit te leggen wat het stuk code precies doet.

Layout
In de MainActivity en de MoreInfoActivity staan onderaan losse classes. Hierdoor wordt de code minder overzichtelijk.
Het wordt overzichtelijker wanneer deze classes in een aparte Java class file worden gezet.

Flow
Voor het lezen van de JSON worden twee verschillende classes gebruikt. De één voor het lezen van een zoekopdracht en de ander voor het lezen van meer informatie over een bepaalde film. Hierin wordt redelijk veel dezelfde code gebruikt.
Deze twee classes hadden beter samengevoegd kunnen worden tot één class (in een aparte Java class file). 