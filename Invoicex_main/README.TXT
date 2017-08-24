Compilazione di Invoicex

Requisiti: NetBeans 8, Java JDK 1.7 e JDK 1.5

Decomprimere il file Invoicex_src_<versione>.zip 
Lo zip contiene diverse cartelle:
- commons-tnx				(libreria di utilità di TNX)
- Invoicex_mercurial		(il progetto Invoicex)
- Invoicex_res				(file risorse, generalmente icone)
- jgoodies-looks-2.4.0b1				(look & feel utilizzato dentro InvoiceX)
- jgoodies-looks-2.4.0b1_nbproject	(progetto del look & feel)
- jide-oss-3.3.5			(libreria jide http://www.jidesoft.com/products/oss.htm di cui si utilizzano alcuni componenti)

Aprire tutti i progetti dentro NetBeans.
Controllare le impostazioni dei progetti cliccando Properties su ogni progetto e verificare:
1-Libraries->Java Platform impostandola su JDK 1.7 (eccetto commons-tnx da impostare su JDK 1.5)
2-Build->Compiling->Additional Compiler Options: se impostati i percorsi del bootclasspath mettere il percorso giusto alle librerie del java 5

Compilare questi progetti in qesto ordine:
- jide-oss-3.3.5
- jgoodies-looks-2.4.0b1_nbproject
- commons-tnx
- Invoicex_res
	 
Controllare i riferimenti alle librerie del progetto Invoicex_mercurial, quindi da Properties->Libraries
se ci sono librerie indicate in rosso rimuoverle e reinserirle.
Ad esempio jide-oss-3.3.5.jar dovrebbe essere in rosso, click su 'Remove', poi click su 'Add Jar/Folder' cercando nella cartella 
jide-oss-3.3.5\dist, il file jide-oss-3.3.5.jar

Eseguire la compilazione di Invoicex_mercurial, verranno evidenziati due warning ma completera la compilazione e troverete in Invoicex_mercurial\dist il file Invoicex.jar