/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.rules.lv;

import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Splitting;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.LanguageSpecificTest;
import org.languagetool.language.Latvian;
import lv.semti.morphology.analyzer.Analyzer;
import org.languagetool.tagging.lv.LatvianSentenceTokenizer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.is;

public class LatvianTest extends LanguageSpecificTest {
  
  @Test
  public void testLanguage() throws IOException {
    runTests(new Latvian());
  }

  @Test
  public void testSpellingAndColon() throws IOException {
    JLanguageTool lt = new JLanguageTool(new Latvian());
    System.out.println(lt.check("Šis ir teikums parāda ka saikļiem ir jāliek komats un ka mans C-vitamīns ir foršs (Ph. D. phys. Džonsons et al)."));
    System.out.println(lt.check("Es sekoju šai lietai cieši jo man tā ir svarīga! LV–1002"));
    assertThat(lt.check("Šis ir  testa teikums.").size(), is(1));

  }


  @Test
  public void testDashRules() throws IOException {
    JLanguageTool lt = new JLanguageTool(new Latvian());
    System.out.println(lt.check("Rīgas-Viļņas vilcieni kursē ļoti reti."));
    System.out.println(lt.check("Rīgas–Viļņas vilcieni kursē ļoti reti."));
    System.out.println(lt.check("Maršrutā Rīga-Roja-Kolka autobusi kursē ļoti reti."));
    System.out.println(lt.check("Spēlē Latvija–Papua-Jaungvineja pašlaik rezultāts ir neizšķirts."));
    System.out.println(lt.check("Spēlē Latvija-Papua-Jaungvineja pašlaik rezultāts ir neizšķirts."));
    assertThat(lt.check("Šis ir  testa teikums.").size(), is(1));
  }


  @Test
  public void testDefiseRules() throws IOException {
    JLanguageTool lt = new JLanguageTool(new Latvian());
    seperate_test(lt, "Juris Balodis–Bolužs");
    seperate_test(lt, "Juris Juško–Boluža");

    seperate_test(lt, "Franču-austriešu karš"); // correct
    // Test maršruti
    seperate_test(lt, "Rīga—Viļņa"); // correct
    seperate_test(lt, "Rīgas – Viļņas"); // false -> no spaces
    // Test uzvārdi
    seperate_test(lt, "Molotova-Ribentropa pakts"); // false -> jālieto domuzīme
    seperate_test(lt, "Molotova–Ribentropa pakts"); // correct
    seperate_test(lt, "Molotova– Ribentropa"); //  false -> no spaces
    seperate_test(lt, "Molotova – Ribentropa"); //  false -> no spaces
    seperate_test(lt, "Vaira Vīķe–Freiberga"); // false -> jālieto defise
    seperate_test(lt, "J. K. Roulinga");
    seperate_test(lt, "a P.S. es nezinu ko es daru\nP.S. nu vēl mazāk");
    seperate_test(lt, "u.t.jpr.");
    // Test multivārdi
    seperate_test(lt, "diode–triode"); // false -> jālieto defise
    seperate_test(lt, "e–pasts?"); // false -> jālieto defise
    // Test tautību/valodu kombinācijas
    seperate_test(lt, "Angļu-krievu-vācu vārdnīca"); // false -> jālieto domuzīme
    seperate_test(lt, "Līgums par padomju-vācu robežu"); // false -> jālieto domuzīme
    seperate_test(lt, "Itāļu–vācu kabatas vārdnīca"); // true
    // Test mēnešu/nedēļas dienu kombinācijas
    seperate_test(lt, "Pagājušā gada janvāra-augusta līmenis"); // false -> jālieto domuzīme
    seperate_test(lt, "Aprīlis-septembris: otrdiena-svētdiena , 10.00-20.00"); // false -> jālieto domuzīme
    // Atdalot divus standarta vārdus
    seperate_test(lt, "Es nevēlos siļķi, Jānis – zupu."); // correct
    seperate_test(lt, "Visi pērk maizīti – bez tās neiztikt –, tomēr gribas arī kādu saldu dzērienu."); // correct -> domuzīmei komats seko uzreiz
    seperate_test(lt, "Lai sāktu strādāt, ir jāpārstāj runāt un jāsāk darīt.\" – Volts Disnejs ");
    // Test
    assertThat(lt.check("Šis ir  testa teikums.").size(), is(1));
  }


  @Test
  public void testDefiseWhitespaceRules() throws IOException {
    JLanguageTool lt = new JLanguageTool(new Latvian());

    // Test maršruti
    seperate_test(lt, "Rīgas–Viļņas"); // correct
    seperate_test(lt, "Rīgas– Viļņas"); //
    seperate_test(lt, "Rīgas –Viļņas"); //
    seperate_test(lt, "Rīgas – Viļņas"); //

    seperate_test(lt, "Rīgas-Viļņas"); // correct
    seperate_test(lt, "Rīgas- Viļņas"); //
    seperate_test(lt, "Rīgas -Viļņas"); //
    seperate_test(lt, "Rīgas - Viļņas"); //

    seperate_test(lt, "Rīgas—Viļņas"); // correct
    seperate_test(lt, "Rīgas— Viļņas"); //
    seperate_test(lt, "Rīgas —Viļņas"); //
    seperate_test(lt, "Rīgas — Viļņas"); //
  }

  @Test
  public void defiseStarpAtstarpem() throws IOException {
    JLanguageTool lt = new JLanguageTool(new Latvian());
    seperate_test(lt, "Man nepatīk e- klase");
//    seperate_test(lt, "Ko tu -, dari?");
//    seperate_test(lt, "Te ir ar vairākām   - atstarpēm.");
//    seperate_test(lt, "Nezinu- varbūt šis arī jāatdala?");
//    seperate_test(lt, "A, ja nu -šis gadās?");
//    seperate_test(lt, "Vai gadījumā nenobrūk viss ja beigās ir defise -");
  }

  @Test
  public void testDacades() throws IOException {
    JLanguageTool lt = new JLanguageTool(new Latvian());
    System.out.println(lt.check("1960.-tie bija traki laiki."));
    System.out.println(lt.check("2010to gadu sākums bija traki laiki."));
    System.out.println(lt.check("80.-90. gadu sākumā bija lieli nemieri."));
    System.out.println(lt.check("1920-tajos gados bija aizliegts"));
    System.out.println(lt.check("1920.-tajos gados bija aizliegts"));
    System.out.println(lt.check("20tajos gados bija aizliegts"));
    System.out.println(lt.check("20.-tajos gados bija aizliegts"));
  }


  @Test
  public void multiDotTest() throws IOException {
    JLanguageTool lt = new JLanguageTool(new Latvian());
    System.out.println(lt.check("Man nepatīk cilvēki..")); // false -> single dot
    System.out.println(lt.check("Man nepatīk cilvēki, kas [..] šeit dzīvo.")); // true
  }


  @Test
  public void numbersMason() throws IOException {
    JLanguageTool lt = new JLanguageTool(new Latvian());
    System.out.println(lt.check("1500")); // false -> single dot
    System.out.println(lt.check("15000")); // true
  }


  @Test
  public void kamerTest() throws IOException {
    JLanguageTool lt = new JLanguageTool(new Latvian());
    System.out.println(lt.check("Pašlaik latviešu valodas runas sintēzes jomā primārais fokuss ir bijis tieši uz dabiskas runas un balss sintēzi, kamēr teksta priekšapstrāde ir sekundārā lomā [1].")); // false -> true
    System.out.println(lt.check("Kora Kamēr uzstāšanās bija burvīga")); // true
    System.out.println(lt.check("Tikmēr kamēr man ir nauda es gribu pirkt visvisādas lietas")); // false -> , kamēr
  }

  @Test
  public void varatTest() throws IOException {
    JLanguageTool lt = new JLanguageTool(new Latvian());
    System.out.println(lt.check("Darba vadītājs: Ph. D. phys. Normunds Grūzītis\n")); // false -> true
    System.out.println(lt.check("Variet par to pārliecināties paši !")); // false -> true
    System.out.println(lt.check("( 2013. g. 17. janvārī)")); // false -> true
  }

  @Test
  public void overlapTest() throws IOException {
    JLanguageTool lt = new JLanguageTool(new Latvian());
    System.out.println(lt.check("\n\nAugstas precizitātes marķētāji sasniedz līdz pat 97% precizitāti, balstoties uz anotētiem korpusiem. Šī marķēšana ir būtiska ne tikai parsēšanai, bet arī citos valodas tehnoloģiju uzdevumos, tostarp tulkošanā un runas atpazīšanā.\n")); // false -> true
    System.out.println(lt.check("Tests ir tests lai pārbaudītu pāris lietas")); // false -> true
    System.out.println(lt.check("Rituma et al. 2019 – Rituma, Laura, Saulīte, Baiba, Nešpore-Bērzkalne, Gunta. Latviešu valodas sintaktiski marķētā korpusa gramatikas modelis. Valoda: nozīme un forma, 10. Rīga :  LU Akadēmiskais apgāds, 2019, 200.–216. lpp. DOI: https://doi.org/10.22364/vnf.10.16"));
  }


  @Test
  public void emojiTest() throws IOException {
    JLanguageTool lt = new JLanguageTool(new Latvian());
    System.out.println("\uD83D\uDE00".length()); // false -> single dot
    System.out.println(lt.check("Man nepatīk cilvēki, kas [..] šeit dzīvo.")); // true
  }


  @Test
  public void quotesTest() throws IOException {
    JLanguageTool lt = new JLanguageTool(new Latvian());
//    System.out.println(lt.check("\"Šis ir tests lai pārbaudītu pāris lietas: \"Man patīk pelmeņi!\"\"\n\n\"Man patīk pelmeņi!\"")); // false -> true
    System.out.println(lt.check(" Pauzes spontānā runā parasti ir sastopamas trīs dažādās pozīcijās (Hansson 1998, 158): 1) starp izteikumiem, kas ir „minimāla intonatīvi pabeigta saziņas vienība, kam var būt noteikts strukturāls paraugs, bet tas nav obligāts, un ko rakstos norobežo ar teikuma beigu pieturzīmi”"));
//    System.out.println(lt.check("LATVIJAS UNIVERSITĀTE\n" +
//      "DATORIKAS FAKULTĀTE\n" +
//      "\n" +
//      "\n" +
//      "\n" +
//      "LATVIEŠU VALODAS TEKSTA AUTOMATIZĒTA APSTRĀDE RUNAS SINTĒZES VAJADZĪBĀM\n" +
//      "\n" +
//      "MAĢISTRA DARBS\n" +
//      "\n" +
//      "\n" +
//      "\n" +
//      "\n" +
//      "\n" +
//      "\n" +
//      "\n" +
//      "\n" +
//      "Autors: Viesturs Jūlijs Lasmanis\n" +
//      "Studenta apliecības Nr.: vl19039\n" +
//      "Darba vadītājs: Dr. sc. comp. Normunds Grūzītis\n" +
//      "\n" +
//      "\n" +
//      "\n" +
//      "Anotācija\n" +
//      "\n" +
//      "Latviešu valodas runas sintēzē ir identificētas dažādas tekstvienības, kurām pareizas izrunas noteikšana sagādā problēmas. Darbā tiek aplūkotas divas pieejas kā automatizēti uzlabot un noteikt nepieciešamās izrunas.\n" +
//      "Darba ietvaros tika izstrādātas un salīdzinātas trīs metodes skaitļa vārdu izvēršanai – sintaktiskās parsēšanas metode un izvēršana pielietojot ChatGPT un mT5 mašīnmācīšanās modeļus. Visaugstākos rezultātus skaitļu izvēršanai kontekstā izdodas panākt pielietojot sintaktiskās parsēšanas metodi, sasniedzot 78,76% un 80% precizitāti uz divām darbā izveidotajām testa datu kopām.\n" +
//      "Vārdu fonētiskajai locīšanai tika veiksmīgi izveidots LVTagger papildinājuma prototips, kas spēj ģenerēt izrunu pāris piemēra vārdiem. Tomēr tiek secināts, ka ir nepieciešama labāka morfoloģiskā parsēšana, lai šo metodi varētu pielietot praktiski.\n" +
//      "\n" +
//      "\n" +
//      "\n" +
//      "Atslēgvārdi: latviešu valoda, teksta normalizēšana, sintaktiskā parsēšana, mašīnmācīšanās, fonētiskā izruna, runas sintēze\n" +
//      "\n" +
//      "\n" +
//      "\n" +
//      "Annotation\n" +
//      "Latvian text automatic processing for speech synthesis purposes\n" +
//      "\n" +
//      "[Translate the annotation]\n" +
//      "\n" +
//      "\n" +
//      "Keywords: Latvian language, text normalization, sytactic parsing, machine learning, phonetic pronunciation, speech synthesis\n" +
//      "\n" +
//      "\n" +
//      "\n" +
//      "Autoreferāts\n" +
//      "\n" +
//      "Darbā autors ir veicis un aprakstījis sekojošās lietas:\n" +
//      "Veikta literatūras izpēte par metodēm, kuras pielieto skaitļu izvēršanai kontekstā \n" +
//      "Izveidoja datu kopu priekš mašīmācīšanās metodes apmācības un testēšanas pielietojot Thrax savēršanas gramatiku.\n" +
//      "Izveidoja datu kopu manuāli izvēršot 100 teikumus no LVK2022 taktu korpusa.\n" +
//      "Izstrādāta sintaktiskās parsēšanas metode, kas sasniedz 78,76% un 80% uz izveidotajām datu kopām.\n" +
//      "Sintaktiski samarķēja LVK2022 tekstu korpusu un analizēja sintaktisko attiecību attālumus.\n" +
//      "Notestēts GPT-3.5-turbo modelis skaitļu izvēršanas uzdevumam, kā arī pielāgots few-shot modelis pielietojot GPT-3.5-turbo. Sasniedzot 60,02% un 50% precizitāti uz few-shot modeli.\n" +
//      "Izstrādāja un apmācīja mT5 pielietojot izveidoto treniņdatu kopu, saniedzot 93,71% un 60% precizitāti uz izveidotājām testa datu kopām.\n" +
//      "Izpētīja latviešu valodai pieejamos vārdu izrunas datus, tostarp latviešu valodas pareizrakstības un pareizrunas vārdnīcu un Tēzaurā pieejamos datus.\n" +
//      "Analizēja LVTagger uzbūvi un nianses, kas sagādā grūtības fonētiskās locīšanas izstrādei. Autors arī aplūkoja veiktus pētījumus saistītus ar LVTagger uzlabojumiem.\n" +
//      "Izstrādāja metodi, kas izgūst izrunas datus no valodnieku labotajiem latviešu valodas pareizrakstības un pareizrunas vārdnīcas, un pārveido tos uz vēlamo fonētiskā pieraksta formātu.\n" +
//      "Izstrādāja papildinājumu LVTagger “morphology” bibliotēkām, kas ir spējīgs ģenerēt fonētisku locījumu dažiem tipiski daudznozīmīgiem vārdiem. Autors apraksta darbā šo papildinājumu izstrādes procesu un pieņemtos lēmumus. \n" +
//      "\n" +
//      "\n" +
//      "Satura rādītājs\n" +
//      "\n" +
//      " TOC \\z \\o \"1-3\" \\u \\hApzīmējumu sarakstsPAGEREF _Toc167039137 \\h6\n" +
//      "IEVADSPAGEREF _Toc167039138 \\h7\n" +
//      "1.Skaitļu izvēršana kontekstāPAGEREF _Toc167039139 \\h9\n" +
//      "1.1.Iespējamo metožu analīzePAGEREF _Toc167039140 \\h9\n" +
//      "1.2.Datu kopas izveidePAGEREF _Toc167039141 \\h13\n" +
//      "1.2.1.Korpusa izvēlePAGEREF _Toc167039142 \\h13\n" +
//      "1.2.2.Teikumu atlase un teikumu pāru izveidePAGEREF _Toc167039143 \\h14\n" +
//      "1.3.Sintaktiskās parsēšanas metodePAGEREF _Toc167039144 \\h17\n" +
//      "1.3.1. Sintaktiskā parsētāja izvēlePAGEREF _Toc167039145 \\h20\n" +
//      "1.3.2. Korpusa sintaktiskā marķēšanaPAGEREF _Toc167039146 \\h21\n" +
//      "1.4.Izvēršana pielietojot lielos valodu modeļusPAGEREF _Toc167039147 \\h25\n" +
//      "1.4.1. Izvēršana pielietojot ChatGPTPAGEREF _Toc167039148 \\h25\n" +
//      "1.4.2. Izvēršana pielietojot mT5PAGEREF _Toc167039149 \\h28\n" +
//      "2.  Fonētiskās izrunas labošanaPAGEREF _Toc167039150 \\h30\n" +
//      "2.1.Pieejamo fonētisko datu analīzePAGEREF _Toc167039151 \\h32\n" +
//      "2.2.LVTagger izvērtēšanaPAGEREF _Toc167039152 \\h34\n" +
//      "2.3.Fonētiskās locīšanas izstrādePAGEREF _Toc167039153 \\h38\n" +
//      "2.3.1.Paradigmu faila pārveidePAGEREF _Toc167039154 \\h38\n" +
//      "2.3.2.Leksikona ielasīšanaPAGEREF _Toc167039155 \\h40\n" +
//      "2.3.3.Miju realizēšanaPAGEREF _Toc167039156 \\h44\n" +
//      "2.3.4.Fonētikas pēcapstrādePAGEREF _Toc167039157 \\h46\n" +
//      "2.3.5.Integrēšana LVTaggerPAGEREF _Toc167039158 \\h47\n" +
//      "REZULTĀTI UN DISKUSIJAPAGEREF _Toc167039159 \\h50\n" +
//      "SECINĀJUMIPAGEREF _Toc167039160 \\h55\n" +
//      "Izmantotā literatūra un avotiPAGEREF _Toc167039161 \\h57\n" +
//      "PAGEREF _Toc167039162 \\hPielikumi60\n" +
//      "\n" +
//      "\n" +
//      "Apzīmējumu saraksts\n")); // false -> true
//    System.out.println(lt.check("\tViens no iemesliem kāpēc nevar mērīt tikai skaitļu izvēršanas precizitāti ir tas, ka mašīnmācīšanās un neirālo tīklu modeļiem, kā piemēram mT5 un ChatGPT modeļiem ir novērojama tendence modificēt arī citas teikuma tekstvienības, kuras nav nepieciešams mainīt. Attiecīgi šāda modeļa rīcība nav vēlama, jo ir nepieciešams saglabāt oriģinālo tekstu. Šī iemesla dēļ GPT-3.5-turbo modelis būtiski augstākus rezultātus sasniedz, ja vaicājumiem pievienojot tekstu “Do not change any tokens that do not contain numbers”. Tabulā 3.1. var novērot, ka bez šī vaicājuma tiek sasniegta 48,86% precizitāte, bet pievienojot 62,22% precizitāte, kas ir būtisks pieaugums."));
//    System.out.println(lt.check("LATVIJAS UNIVERSITĀTE\t\\tViens no iemesliem kāpēc nevar mērīt tikai skaitļu izvēršanas precizitāti ir tas, ka mašīnmācīšanās un neirālo tīklu modeļiem, kā piemēram mT5 un ChatGPT modeļiem ir novērojama tendence modificēt arī citas teikuma tekstvienības, kuras nav nepieciešams mainīt. Attiecīgi šāda modeļa rīcība nav vēlama, jo ir nepieciešams saglabāt oriģinālo tekstu. Šī iemesla dēļ GPT-3.5-turbo modelis būtiski augstākus rezultātus sasniedz, ja vaicājumiem pievienojot tekstu “Do not change any tokens that do not contain numbers”. Tabulā 3.1. var novērot, ka bez šī vaicājuma tiek sasniegta 48,86% precizitāte, bet pievienojot 62,22% precizitāte, kas ir būtisks pieaugums.\tMan nav svarīgi, kas ar “morphology bibliotēku notiek.")); // false -> true
  }


  @Test
  public void testWeekdayAndMonthNames() throws IOException {
    JLanguageTool lt = new JLanguageTool(new Latvian());
    assertThat(lt.check("På måndag är alla lediga.").size(), is(0));
    assertThat(lt.check("På Måndag är alla lediga.").size(), is(1));
    assertThat(lt.check("Onsdag är lillördag på många håll.").size(), is(0));
    assertThat(lt.check("I oktober kommer ofta den första snön.").size(), is(0));
    assertThat(lt.check("I Oktober kommer ofta den första snön.").size(), is(1));
    assertThat(lt.check("Septembers färger är sköna.").size(), is(0));
  }


  @Test
  public void analyzerTest() throws IOException {
    Analyzer analyzer = null;
    try {
      String text = "Darba vadītājs: Ph. D. phys. Normunds Grūzītis.\n";
      analyzer = new Analyzer();
      LatvianSentenceTokenizer bob = new LatvianSentenceTokenizer(analyzer);
      List<String> sentences = bob.tokenize(text);

      for (String sent: sentences) {
        System.out.println(sent);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void seperate_test(JLanguageTool lt, String test) throws IOException {
    System.out.println(lt.check(test));
    System.out.println("__________");
  }
}
