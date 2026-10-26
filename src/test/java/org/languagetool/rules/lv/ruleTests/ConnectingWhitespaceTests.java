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
package org.languagetool.rules.lv.ruleTests;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.LanguageSpecificTest;
import org.languagetool.language.Latvian;

import java.io.IOException;

import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.is;


public class ConnectingWhitespaceTests extends LanguageSpecificTest  {

  @Test
  public void testLanguage() throws IOException {
    runTests(new Latvian());
  }

  @Test
  public void connectingWhitespace() throws IOException{
    JLanguageTool lt = new JLanguageTool(new Latvian());
    // pirms domuzīmes (vienmēr, izņemot dažus tālāk uzskaitītos gadījumus)
    System.out.println(lt.check("Es nevēlos siļķi, Pēteris – zupu."));
    System.out.println(lt.check("Es nevēlos siļķi, Pēteris  — zupu."));

    // aiz pamata (1) vai kārtas (1.) skaitļa vārda, ja tam seko vārds ar mazu burtu vai daži simboli (%)
    // TODO: Visas naudas vienības aiz cipara: https://www.valodaskonsultacijas.lv/lv/search?search%5Bling_sec%5D=458
    System.out.println(lt.check("2022. gada 30. jūlijs"));
    System.out.println(lt.check("5. pants"));
    System.out.println(lt.check("10 lappuses"));
    System.out.println(lt.check("50 %"));
    System.out.println(lt.check("25 €"));
    System.out.println(lt.check("$25"));
    System.out.println(lt.check("40 eiro 40"));
    System.out.println(lt.check("EUR56.87"));
    System.out.println("2556263");
    System.out.println(lt.check("https://doi.org/10.22364/vnf.10.16"));
    System.out.println(lt.check("2556263"));
    System.out.println(lt.check("EUR1000000.00"));
    System.out.println(lt.check("6 EUR 25 centi"));


    // aiz iniciāļa un dažiem saīsinājumiem
    System.out.println(lt.check("no 1920. līdz 1960.gadam.\n" +
            "L.Ceplīša izstrādātās normas paliek spēkā."));

    // starp saīsinājuma vienībām
    System.out.println(lt.check("Pēteru, Miķeļu u.c. vārdadienām"));

    // sākot no piecciparu skaitļiem – atstarpe pēc katriem trim cipariem skaitot no labās
    System.out.println(lt.check("20 000"));

    System.out.println(lt.check("20.5° C"));
    System.out.println(lt.check("Ārā ir + 10 ° pēc Celsija"));

    System.out.println(lt.check("Ārā ir 10 km /h ātrs vējš!"));
  }

  @Test
  public void connectingWhitespaceTest() throws IOException{
    JLanguageTool lt = new JLanguageTool(new Latvian());
    System.out.println(lt.check("J. Endzelīns mācījās Kauguru pagastskolā (1881‒ 1884), Valmieras apriņķa skolā (1885-1888), Rīgas pilsētas ģimnāzijā (1888 –1892)."));
  }

}
