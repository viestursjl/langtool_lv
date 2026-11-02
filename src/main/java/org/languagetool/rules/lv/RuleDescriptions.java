package org.languagetool.rules.lv;

public class RuleDescriptions {

    // Dashes and hyphen related errors
    public final static String dashFormatMessage = "Domuzīme jālieto vienotājzīmes funkcijā (bez atstarpēm starp domuzīmi un vārdiem), ja tiek nosaukti pretējie poli, sistēmas elementi, galapunkti, gadu intervāls vai tiek savienoti divu dažādu cilvēku uzvārdi.";
    public final static String dashUsageMessage = "Domuzīmi lieto vienotājzīmes funkcijā (bez atstarpēm starp vārdiem), ja nosauc pretējos polus, sistēmas elementus, galapunktus.";
    public final static String dashMessage = "Domuzīme šajā kontekstā.";
    public final static String hyphenMessage = "Defise šajā kontekstā.";
    public final static String dateFormatMessage = "Datumu formatējuma labojums.";
    public final static String whitespaceMessage = "Lieks atstarpes lietojums.";

    public final static String doubleSurnameMessage = "Dubultuzvārdos jālieto defise.";
    public final static String doublePlacenameMessage = "No diviem vārdiem veidotu ģeogrāfisku nosaukumu rakstībā jālieto defise.";
    public final static String letterCodeMessage = "Saīsinājumos un burtkopu izlaidumos jālieto defise.";

    public final static String mDashMessage = "Pārgarās domuzīmes vietā jālieto parastā domuzīme.";
    public final static String quoteDashMessage = "Tiešo runu rindkopas sākumā ievada ar domuzīmi.";


    // Whitespace usage related errors
    public final static String NBSPMessage = "Lai vienības netiktu sadalītas, pārnesot jaunā rindā, vēlams lietot saistīto atstarpi.";
    public final static String NBSPDashMessage = "Pirms domuzīmes vēlams lietot saistīto atstarpi.";
    public final static String NBSPUnitsMessage = "Aiz pamata vai kārtas skaitļa vārda, ja tam seko vārds ar mazu burtu vai simboli, vēlams lietot saistīto atstarpi.";
    public final static String NBSPInitialMessage = "Aiz iniciāļa vēlams lietot saistīto atstarpi.";
    public final static String NBSPAbbrevMessage = "Aiz saīsinājumiem vēlams lietot saistīto atstarpi.";
    public final static String NBSPAbbrevChainMessage = "Starp saīsinājuma vienībām vēlams lietot saistīto atstarpi.";
    public final static String NBSPNumbersMessage = "Sākot no piecciparu skaitļiem jālieto saistītā atstarpe pēc katriem trim cipariem, skaitot no labās puses. ";
    public final static String NBSPTemperatureMessage = "Rakstot temperatūras mērījumu kopā ar skalas nosaukumu, aiz skaitļa liekama saistītā atstarpe Celsija vai Fārenheita grāda apzīmējumos.";
    public final static String DegreesMessage = "Grādu apzīmēšanai lietojams simbols ° bez atstarpes aiz skaitļa.";

    // Grammar and style
    public final static String currencyMessage = "Ieteicams konsekventi ievērot teksta vienību loģisko secību latviešu valodā un visus valūtas vienību nosaukumus norādīt aiz summas ar saistīto atstarpi";
    public final static String varatMessage = "Šajā kontekstā galotnes -iet vietā jālieto -at";
    public final static String decadeFormatMessage = "Ieteicams sekojošais desmitgažu pieraksta formāts.";

    // Bracket and quote usage related errors
    public final static String quoteStyleMessage = "Latviešu valodā formālos tekstos vēlams lietot augšējās atverošās un aizverošās pēdiņas – “piemērs”.";
    public final static String quoteOpenMissingMessage = "Iztrūkstošas atverošās pēdiņas.";
    public final static String quoteClosedMissingMessage = "Iztrūkstošas aizverošās pēdiņas.";
    public final static String bracketsOpenMissingMessage = "Iztrūkstošas atverošās iekavas.";
    public final static String bracketsClosedMissingMessage = "Iztrūkstošas aizverošās iekavas.";

    // Punctuation and style
    public final static String singleInitialMessage = "";
    public final static String symbolGroupMessage = "";
    public final static String doublePunctuationMessage = "Lieka interpunkcijas zīme.";
    public final static String duplicateWordMessage = "Vairākkārt secīgi lietots viens un tas pats vārds.";

}






