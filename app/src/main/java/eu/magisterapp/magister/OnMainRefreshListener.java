package eu.magisterapp.magister;

/**
 * Created by max on 8-1-16.
 */
public interface OnMainRefreshListener {

    /**
     * Deze shit wordt niet op de UI-thread uitgevoerd.
     * Je kan hier dus db queries op uitvoeren. (kan ook wel op ui
     * maar dan skip je ~30 frames, en dat is voor iPhones)
     *
     * Dat betekend echter dat je hiermee geen bewerkingen kan doen
     * op de UI.
     *
     * Houd er rekening mee dat onCreateView of onCreate nog niet
     * geroepen is op de fragments die dit implementen. Deze method
     * wordt aangeroepen in main, op setFragment. Er kunnen dus nullpointers
     * ontstaan omdat je shit gebruikt die je pas in onCreateView of onCreate
     * een waarde geeft.
     */
    public void onRefreshed(MagisterApp app);

    /**
     * Dit ding wordt wel op de UI-thread uitgevoerd, en altijd na onRefreshed().
     * Als je refresh voordat een fragment is gezien door een gebruiker, echter,
     * wordt dit nogsteeds uitgevoerd voor onCreateView. Je moet dus zorgen
     * dat je geen npe's krijgt..
     */
    public void onPostRefresh();


    /**
     * Hier kun je dingen uit cache halen.. wordt als eerste in refresh ding geroepen.
     * @param app
     */
    public Object[] quickUpdate(MagisterApp app);

    public void onQuickUpdated(Object... result);

}
