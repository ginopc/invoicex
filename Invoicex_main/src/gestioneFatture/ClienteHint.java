package gestioneFatture;


public class ClienteHint {

    public String codice;
    public String ragione_sociale;
    public boolean obsoleto = false;

    public ClienteHint() {
        
    }
    
    public ClienteHint(String codice, String ragione_sociale) {
        this.codice = codice;
        this.ragione_sociale = ragione_sociale;
    }

    @Override
    public String toString() {
        String ret = "";
        ret = ragione_sociale + " [" + codice + "]";
        return ret;
    }
}