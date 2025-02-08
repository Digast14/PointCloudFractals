Hi Erixon, du solltest das Programm öffnen können, dependencies sollten automatisch runtergeladen werden. Wenn dus startest Hast dann Widget und denn Main screen. In 2D Steuerung WASD zum Bewegen, QE zum Zoomen, mit 1 2 3 kannst Render modus wechseln. 
Bei Input kannst Funktionen eingeben. So wie in Geogebra, halt ohne dem f(x)... Die unbekannte Variable ist auch nicht x sondern q. Du kannst auch c eingeben das ist die Pixel beziehungsweise die VoxelCoordinate. 
Außerdem gibt es die Variablen t und n, t ist ein FLoat zwischen -1 und 1, n kannst mit dem Regler entscheiden.
Qzero ändert den start wert der Funktion, standardmäßig ist es c. Falls du die c inder Funktion hat ist 0 meistens besser.
Du kannst auch Quaternions in die Funktionen schreiben, dafür aber quant(x,y,z,w) wobei x,y,z,w floats sind, kannst aber auch n oder c benutzen
q und c sind von sich quaternions. Nach ^ kannst auch ein quaternion hinschreiben, dann wird aber nur die ^ x gerechnet
Spezielle Funktionen die du wie in geogebra einbauen kannst sind:
sin(q) ...q ist irgendein quaternion
cos(q) ...q ist irgendein quaternion
exp(q) ...q ist irgendein quaternion
ln(q) ...q ist irgendein quaternion

man kann in die funktionen aber auch einfach zahlen reinschreiben.
Beispiele Für Funktionen:

q^2+c
sin(q)
sin(q^2)+c
q-quant(1,1,0,0)*(q^3-1)/(3*q^2)
q-quant(1,1,t,0)*(q^n-c)/(n*q^(n-1))

Wenn du eine neue Funktion auch eingben willst musst du den "compile Function" knopf drücken
Timer auto macht das t automatisch zwischen -1 und 1 wandert
Color sind einfach die Farben

Bei Shader Settings kannst spezifische Einstellungen ändern. 
Max iteration:     gibt an wie häufig die Funktion angwendet werden soll. (Für 2D kannst dus ruhig in zwischen 100 und 1000 stellen)
nudge Value:       wie genau die Intersection ist. Standard (0.0001) ist meistens gut genug
Breakout Factor:   wie ob es ein limit gibt ab wann die Intersection gezählt wird (Für kleine Max Iteration ist manchmal höher besser)

mit Local Point Cloud Fractal kommst ins 3D.
Steuerung ist WASD Shift Space, Rechtklick gedrückt halten und Maus bewegen zum umschauen. Man bewegst sich immer relative zu kamera Richtung.

Shader Settings neu:

Range:    Gibt die größe von der PointCloud an 
normal Precision: Gibt and wie viele Iterationen der NormalVektor benutzt
normal step size:  Gibt an wie groß der Radius vom Normal Vektor Feld ist
Quadsize, ändert die größe der Quads
Resolution: ändert die Dichte und Anzahl der Punkte (oben stehen die Punkte, Probier ruhig aus es höher zu stellen, mein PC schafft bei manchen Funktionen bis zu 80)
invert: invertiert die Punkte, also überall wo kein Punkt ist, kommt ein Punkt, und überall wo einer ist verschwinder er.
Blur: Macht das Bild verschowemmen, nicht sehr nützlich

In 2d sind abgesehen vom Funktionen ändern alles in realtime bearbeitbar, in 3D nur die hintergrund Farbe und die Quadsize. Alles andere brauch ein Recompile.
Für coole 3d Point Clouds, emphele ich übrigens mit einem quantor zu multplizieren. 
zB:
quant(1,1,0,0) * ... 
quant(1,0,1,0) * ...
etc.

Du kannst das Widget übrigens auch aus dem Sreen raus bwegen.

Falls noch Fragen hast DM mich.

