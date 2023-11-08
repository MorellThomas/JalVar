/*
 * Jalview - A Sequence Alignment Editor and Viewer ($$Version-Rel$$)
 * Copyright (C) $$Year-Rel$$ The Jalview Authors
 * 
 * This file is part of Jalview.
 * 
 * Jalview is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * Jalview is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Jalview.  If not, see <http://www.gnu.org/licenses/>.
 * The Jalview Authors are detailed in the 'AUTHORS' file.
 */
package jalview.gui.structurechooser;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Vector;

import org.junit.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.fts.core.FTSRestResponse;
import jalview.fts.service.pdb.PDBFTSRestClientTest;
import jalview.fts.threedbeacons.TDBeaconsFTSRestClientTest;
import jalview.gui.JvOptionPane;
import jalview.gui.StructureChooser;
import jalview.jbgui.FilterOption;

public class StructureChooserQuerySourceTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  Sequence seq, upSeq, upSeq_insulin, upSeq_r1ab;

  private Sequence upSeq_fer1_maize;

  // same set up as for structurechooser test

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception
  {
    seq = new Sequence("PDB|4kqy|4KQY|A", "ABCDEFGHIJKLMNOPQRSTUVWXYZ", 1,
            26);
    seq.createDatasetSequence();
    for (int x = 1; x < 5; x++)
    {
      DBRefEntry dbRef = new DBRefEntry();
      dbRef.setAccessionId("XYZ_" + x);
      seq.addDBRef(dbRef);
    }

    PDBEntry dbRef = new PDBEntry();
    dbRef.setId("1tim");

    Vector<PDBEntry> pdbIds = new Vector<>();
    pdbIds.add(dbRef);

    seq.setPDBId(pdbIds);

    // Uniprot sequence for 3D-Beacons mocks
    upSeq = new Sequence("P38398",
            "MDLSALRVEEVQNVINAMQKILECPICLELIKEPVSTKCDHIFCKFCMLKLLNQKKGPSQCPLCKNDITKRS\n"
                    + "LQESTRFSQLVEELLKIICAFQLDTGLEYANSYNFAKKENNSPEHLKDEVSIIQSMGYRNRAKRLLQSEPEN\n"
                    + "PSLQETSLSVQLSNLGTVRTLRTKQRIQPQKTSVYIELGSDSSEDTVNKATYCSVGDQELLQITPQGTRDEI\n"
                    + "SLDSAKKAACEFSETDVTNTEHHQPSNNDLNTTEKRAAERHPEKYQGSSVSNLHVEPCGTNTHASSLQHENS\n"
                    + "SLLLTKDRMNVEKAEFCNKSKQPGLARSQHNRWAGSKETCNDRRTPSTEKKVDLNADPLCERKEWNKQKLPC\n"
                    + "SENPRDTEDVPWITLNSSIQKVNEWFSRSDELLGSDDSHDGESESNAKVADVLDVLNEVDEYSGSSEKIDLL\n"
                    + "ASDPHEALICKSERVHSKSVESNIEDKIFGKTYRKKASLPNLSHVTENLIIGAFVTEPQIIQERPLTNKLKR\n"
                    + "KRRPTSGLHPEDFIKKADLAVQKTPEMINQGTNQTEQNGQVMNITNSGHENKTKGDSIQNEKNPNPIESLEK\n"
                    + "ESAFKTKAEPISSSISNMELELNIHNSKAPKKNRLRRKSSTRHIHALELVVSRNLSPPNCTELQIDSCSSSE\n"
                    + "EIKKKKYNQMPVRHSRNLQLMEGKEPATGAKKSNKPNEQTSKRHDSDTFPELKLTNAPGSFTKCSNTSELKE\n"
                    + "FVNPSLPREEKEEKLETVKVSNNAEDPKDLMLSGERVLQTERSVESSSISLVPGTDYGTQESISLLEVSTLG\n"
                    + "KAKTEPNKCVSQCAAFENPKGLIHGCSKDNRNDTEGFKYPLGHEVNHSRETSIEMEESELDAQYLQNTFKVS\n"
                    + "KRQSFAPFSNPGNAEEECATFSAHSGSLKKQSPKVTFECEQKEENQGKNESNIKPVQTVNITAGFPVVGQKD\n"
                    + "KPVDNAKCSIKGGSRFCLSSQFRGNETGLITPNKHGLLQNPYRIPPLFPIKSFVKTKCKKNLLEENFEEHSM\n"
                    + "SPEREMGNENIPSTVSTISRNNIRENVFKEASSSNINEVGSSTNEVGSSINEIGSSDENIQAELGRNRGPKL\n"
                    + "NAMLRLGVLQPEVYKQSLPGSNCKHPEIKKQEYEEVVQTVNTDFSPYLISDNLEQPMGSSHASQVCSETPDD\n"
                    + "LLDDGEIKEDTSFAENDIKESSAVFSKSVQKGELSRSPSPFTHTHLAQGYRRGAKKLESSEENLSSEDEELP\n"
                    + "CFQHLLFGKVNNIPSQSTRHSTVATECLSKNTEENLLSLKNSLNDCSNQVILAKASQEHHLSEETKCSASLF\n"
                    + "SSQCSELEDLTANTNTQDPFLIGSSKQMRHQSESQGVGLSDKELVSDDEERGTGLEENNQEEQSMDSNLGEA\n"
                    + "ASGCESETSVSEDCSGLSSQSDILTTQQRDTMQHNLIKLQQEMAELEAVLEQHGSQPSNSYPSIISDSSALE\n"
                    + "DLRNPEQSTSEKAVLTSQKSSEYPISQNPEGLSADKFEVSADSSTSKNKEPGVERSSPSKCPSLDDRWYMHS\n"
                    + "CSGSLQNRNYPSQEELIKVVDVEEQQLEESGPHDLTETSYLPRQDLEGTPYLESGISLFSDDPESDPSEDRA\n"
                    + "PESARVGNIPSSTSALKVPQLKVAESAQSPAAAHTTDTAGYNAMEESVSREKPELTASTERVNKRMSMVVSG\n"
                    + "LTPEEFMLVYKFARKHHITLTNLITEETTHVVMKTDAEFVCERTLKYFLGIAGGKWVVSYFWVTQSIKERKM\n"
                    + "LNEHDFEVRGDVVNGRNHQGPKRARESQDRKIFRGLEICCYGPFTNMPTDQLEWMVQLCGASVVKELSSFTL\n"
                    + "GTGVHPIVVVQPDAWTEDNGFHAIGQMCEAPVVTREWVLDSVALYQCQELDTYLIPQIPHSHY\n"
                    + "",
            1, 1863);
    upSeq.createDatasetSequence();
    upSeq.setDescription("Breast cancer type 1 susceptibility protein");
    upSeq.addDBRef(new DBRefEntry("UNIPROT", "0", "P38398", null, true));

    upSeq_insulin = new Sequence("INS_HUMAN",
            "MALWMRLLPLLALLALWGPDPAAAFVNQHLCGSHLVEALYLVCGERGFFYTPKTRREAEDLQVGQVELGGGP"
                    + "GAGSLQPLALEGSLQKRGIVEQCCTSICSLYQLENYCN");
    upSeq_insulin.createDatasetSequence();
    upSeq_insulin.setDescription("Insulin");
    upSeq_insulin
            .addDBRef(new DBRefEntry("UNIPROT", "0", "P01308", null, true));

    upSeq_r1ab = new Sequence("R1AB_SARS2",
            "MESLVPGFNEKTHVQLSLPVLQVRDVLVRGFGDSVEEVLSEARQHLKDGTCGLVEVEKGVLPQLEQPYVFIK\n"
                    + "RSDARTAPHGHVMVELVAELEGIQYGRSGETLGVLVPHVGEIPVAYRKVLLRKNGNKGAGGHSYGADLKSFD\n"
                    + "LGDELGTDPYEDFQENWNTKHSSGVTRELMRELNGGAYTRYVDNNFCGPDGYPLECIKDLLARAGKASCTLS\n"
                    + "EQLDFIDTKRGVYCCREHEHEIAWYTERSEKSYELQTPFEIKLAKKFDTFNGECPNFVFPLNSIIKTIQPRV\n"
                    + "EKKKLDGFMGRIRSVYPVASPNECNQMCLSTLMKCDHCGETSWQTGDFVKATCEFCGTENLTKEGATTCGYL\n"
                    + "PQNAVVKIYCPACHNSEVGPEHSLAEYHNESGLKTILRKGGRTIAFGGCVFSYVGCHNKCAYWVPRASANIG\n"
                    + "CNHTGVVGEGSEGLNDNLLEILQKEKVNINIVGDFKLNEEIAIILASFSASTSAFVETVKGLDYKAFKQIVE\n"
                    + "SCGNFKVTKGKAKKGAWNIGEQKSILSPLYAFASEAARVVRSIFSRTLETAQNSVRVLQKAAITILDGISQY\n"
                    + "SLRLIDAMMFTSDLATNNLVVMAYITGGVVQLTSQWLTNIFGTVYEKLKPVLDWLEEKFKEGVEFLRDGWEI\n"
                    + "VKFISTCACEIVGGQIVTCAKEIKESVQTFFKLVNKFLALCADSIIIGGAKLKALNLGETFVTHSKGLYRKC\n"
                    + "VKSREETGLLMPLKAPKEIIFLEGETLPTEVLTEEVVLKTGDLQPLEQPTSEAVEAPLVGTPVCINGLMLLE\n"
                    + "IKDTEKYCALAPNMMVTNNTFTLKGGAPTKVTFGDDTVIEVQGYKSVNITFELDERIDKVLNEKCSAYTVEL\n"
                    + "GTEVNEFACVVADAVIKTLQPVSELLTPLGIDLDEWSMATYYLFDESGEFKLASHMYCSFYPPDEDEEEGDC\n"
                    + "EEEEFEPSTQYEYGTEDDYQGKPLEFGATSAALQPEEEQEEDWLDDDSQQTVGQQDGSEDNQTTTIQTIVEV\n"
                    + "QPQLEMELTPVVQTIEVNSFSGYLKLTDNVYIKNADIVEEAKKVKPTVVVNAANVYLKHGGGVAGALNKATN\n"
                    + "NAMQVESDDYIATNGPLKVGGSCVLSGHNLAKHCLHVVGPNVNKGEDIQLLKSAYENFNQHEVLLAPLLSAG\n"
                    + "IFGADPIHSLRVCVDTVRTNVYLAVFDKNLYDKLVSSFLEMKSEKQVEQKIAEIPKEEVKPFITESKPSVEQ\n"
                    + "RKQDDKKIKACVEEVTTTLEETKFLTENLLLYIDINGNLHPDSATLVSDIDITFLKKDAPYIVGDVVQEGVL\n"
                    + "TAVVIPTKKAGGTTEMLAKALRKVPTDNYITTYPGQGLNGYTVEEAKTVLKKCKSAFYILPSIISNEKQEIL\n"
                    + "GTVSWNLREMLAHAEETRKLMPVCVETKAIVSTIQRKYKGIKIQEGVVDYGARFYFYTSKTTVASLINTLND\n"
                    + "LNETLVTMPLGYVTHGLNLEEAARYMRSLKVPATVSVSSPDAVTAYNGYLTSSSKTPEEHFIETISLAGSYK\n"
                    + "DWSYSGQSTQLGIEFLKRGDKSVYYTSNPTTFHLDGEVITFDNLKTLLSLREVRTIKVFTTVDNINLHTQVV\n"
                    + "DMSMTYGQQFGPTYLDGADVTKIKPHNSHEGKTFYVLPNDDTLRVEAFEYYHTTDPSFLGRYMSALNHTKKW\n"
                    + "KYPQVNGLTSIKWADNNCYLATALLTLQQIELKFNPPALQDAYYRARAGEAANFCALILAYCNKTVGELGDV\n"
                    + "RETMSYLFQHANLDSCKRVLNVVCKTCGQQQTTLKGVEAVMYMGTLSYEQFKKGVQIPCTCGKQATKYLVQQ\n"
                    + "ESPFVMMSAPPAQYELKHGTFTCASEYTGNYQCGHYKHITSKETLYCIDGALLTKSSEYKGPITDVFYKENS\n"
                    + "YTTTIKPVTYKLDGVVCTEIDPKLDNYYKKDNSYFTEQPIDLVPNQPYPNASFDNFKFVCDNIKFADDLNQL\n"
                    + "TGYKKPASRELKVTFFPDLNGDVVAIDYKHYTPSFKKGAKLLHKPIVWHVNNATNKATYKPNTWCIRCLWST\n"
                    + "KPVETSNSFDVLKSEDAQGMDNLACEDLKPVSEEVVENPTIQKDVLECNVKTTEVVGDIILKPANNSLKITE\n"
                    + "EVGHTDLMAAYVDNSSLTIKKPNELSRVLGLKTLATHGLAAVNSVPWDTIANYAKPFLNKVVSTTTNIVTRC\n"
                    + "LNRVCTNYMPYFFTLLLQLCTFTRSTNSRIKASMPTTIAKNTVKSVGKFCLEASFNYLKSPNFSKLINIIIW\n"
                    + "FLLLSVCLGSLIYSTAALGVLMSNLGMPSYCTGYREGYLNSTNVTIATYCTGSIPCSVCLSGLDSLDTYPSL\n"
                    + "ETIQITISSFKWDLTAFGLVAEWFLAYILFTRFFYVLGLAAIMQLFFSYFAVHFISNSWLMWLIINLVQMAP\n"
                    + "ISAMVRMYIFFASFYYVWKSYVHVVDGCNSSTCMMCYKRNRATRVECTTIVNGVRRSFYVYANGGKGFCKLH\n"
                    + "NWNCVNCDTFCAGSTFISDEVARDLSLQFKRPINPTDQSSYIVDSVTVKNGSIHLYFDKAGQKTYERHSLSH\n"
                    + "FVNLDNLRANNTKGSLPINVIVFDGKSKCEESSAKSASVYYSQLMCQPILLLDQALVSDVGDSAEVAVKMFD\n"
                    + "AYVNTFSSTFNVPMEKLKTLVATAEAELAKNVSLDNVLSTFISAARQGFVDSDVETKDVVECLKLSHQSDIE\n"
                    + "VTGDSCNNYMLTYNKVENMTPRDLGACIDCSARHINAQVAKSHNIALIWNVKDFMSLSEQLRKQIRSAAKKN\n"
                    + "NLPFKLTCATTRQVVNVVTTKIALKGGKIVNNWLKQLIKVTLVFLFVAAIFYLITPVHVMSKHTDFSSEIIG\n"
                    + "YKAIDGGVTRDIASTDTCFANKHADFDTWFSQRGGSYTNDKACPLIAAVITREVGFVVPGLPGTILRTTNGD\n"
                    + "FLHFLPRVFSAVGNICYTPSKLIEYTDFATSACVLAAECTIFKDASGKPVPYCYDTNVLEGSVAYESLRPDT\n"
                    + "RYVLMDGSIIQFPNTYLEGSVRVVTTFDSEYCRHGTCERSEAGVCVSTSGRWVLNNDYYRSLPGVFCGVDAV\n"
                    + "NLLTNMFTPLIQPIGALDISASIVAGGIVAIVVTCLAYYFMRFRRAFGEYSHVVAFNTLLFLMSFTVLCLTP\n"
                    + "VYSFLPGVYSVIYLYLTFYLTNDVSFLAHIQWMVMFTPLVPFWITIAYIICISTKHFYWFFSNYLKRRVVFN\n"
                    + "GVSFSTFEEAALCTFLLNKEMYLKLRSDVLLPLTQYNRYLALYNKYKYFSGAMDTTSYREAACCHLAKALND\n"
                    + "FSNSGSDVLYQPPQTSITSAVLQSGFRKMAFPSGKVEGCMVQVTCGTTTLNGLWLDDVVYCPRHVICTSEDM\n"
                    + "LNPNYEDLLIRKSNHNFLVQAGNVQLRVIGHSMQNCVLKLKVDTANPKTPKYKFVRIQPGQTFSVLACYNGS\n"
                    + "PSGVYQCAMRPNFTIKGSFLNGSCGSVGFNIDYDCVSFCYMHHMELPTGVHAGTDLEGNFYGPFVDRQTAQA\n"
                    + "AGTDTTITVNVLAWLYAAVINGDRWFLNRFTTTLNDFNLVAMKYNYEPLTQDHVDILGPLSAQTGIAVLDMC\n"
                    + "ASLKELLQNGMNGRTILGSALLEDEFTPFDVVRQCSGVTFQSAVKRTIKGTHHWLLLTILTSLLVLVQSTQW\n"
                    + "SLFFFLYENAFLPFAMGIIAMSAFAMMFVKHKHAFLCLFLLPSLATVAYFNMVYMPASWVMRIMTWLDMVDT\n"
                    + "SLSGFKLKDCVMYASAVVLLILMTARTVYDDGARRVWTLMNVLTLVYKVYYGNALDQAISMWALIISVTSNY\n"
                    + "SGVVTTVMFLARGIVFMCVEYCPIFFITGNTLQCIMLVYCFLGYFCTCYFGLFCLLNRYFRLTLGVYDYLVS\n"
                    + "TQEFRYMNSQGLLPPKNSIDAFKLNIKLLGVGGKPCIKVATVQSKMSDVKCTSVVLLSVLQQLRVESSSKLW\n"
                    + "AQCVQLHNDILLAKDTTEAFEKMVSLLSVLLSMQGAVDINKLCEEMLDNRATLQAIASEFSSLPSYAAFATA\n"
                    + "QEAYEQAVANGDSEVVLKKLKKSLNVAKSEFDRDAAMQRKLEKMADQAMTQMYKQARSEDKRAKVTSAMQTM\n"
                    + "LFTMLRKLDNDALNNIINNARDGCVPLNIIPLTTAAKLMVVIPDYNTYKNTCDGTTFTYASALWEIQQVVDA\n"
                    + "DSKIVQLSEISMDNSPNLAWPLIVTALRANSAVKLQNNELSPVALRQMSCAAGTTQTACTDDNALAYYNTTK\n"
                    + "GGRFVLALLSDLQDLKWARFPKSDGTGTIYTELEPPCRFVTDTPKGPKVKYLYFIKGLNNLNRGMVLGSLAA\n"
                    + "TVRLQAGNATEVPANSTVLSFCAFAVDAAKAYKDYLASGGQPITNCVKMLCTHTGTGQAITVTPEANMDQES\n"
                    + "FGGASCCLYCRCHIDHPNPKGFCDLKGKYVQIPTTCANDPVGFTLKNTVCTVCGMWKGYGCSCDQLREPMLQ\n"
                    + "SADAQSFLNRVCGVSAARLTPCGTGTSTDVVYRAFDIYNDKVAGFAKFLKTNCCRFQEKDEDDNLIDSYFVV\n"
                    + "KRHTFSNYQHEETIYNLLKDCPAVAKHDFFKFRIDGDMVPHISRQRLTKYTMADLVYALRHFDEGNCDTLKE\n"
                    + "ILVTYNCCDDDYFNKKDWYDFVENPDILRVYANLGERVRQALLKTVQFCDAMRNAGIVGVLTLDNQDLNGNW\n"
                    + "YDFGDFIQTTPGSGVPVVDSYYSLLMPILTLTRALTAESHVDTDLTKPYIKWDLLKYDFTEERLKLFDRYFK\n"
                    + "YWDQTYHPNCVNCLDDRCILHCANFNVLFSTVFPPTSFGPLVRKIFVDGVPFVVSTGYHFRELGVVHNQDVN\n"
                    + "LHSSRLSFKELLVYAADPAMHAASGNLLLDKRTTCFSVAALTNNVAFQTVKPGNFNKDFYDFAVSKGFFKEG\n"
                    + "SSVELKHFFFAQDGNAAISDYDYYRYNLPTMCDIRQLLFVVEVVDKYFDCYDGGCINANQVIVNNLDKSAGF\n"
                    + "PFNKWGKARLYYDSMSYEDQDALFAYTKRNVIPTITQMNLKYAISAKNRARTVAGVSICSTMTNRQFHQKLL\n"
                    + "KSIAATRGATVVIGTSKFYGGWHNMLKTVYSDVENPHLMGWDYPKCDRAMPNMLRIMASLVLARKHTTCCSL\n"
                    + "SHRFYRLANECAQVLSEMVMCGGSLYVKPGGTSSGDATTAYANSVFNICQAVTANVNALLSTDGNKIADKYV\n"
                    + "RNLQHRLYECLYRNRDVDTDFVNEFYAYLRKHFSMMILSDDAVVCFNSTYASQGLVASIKNFKSVLYYQNNV\n"
                    + "FMSEAKCWTETDLTKGPHEFCSQHTMLVKQGDDYVYLPYPDPSRILGAGCFVDDIVKTDGTLMIERFVSLAI\n"
                    + "DAYPLTKHPNQEYADVFHLYLQYIRKLHDELTGHMLDMYSVMLTNDNTSRYWEPEFYEAMYTPHTVLQAVGA\n"
                    + "CVLCNSQTSLRCGACIRRPFLCCKCCYDHVISTSHKLVLSVNPYVCNAPGCDVTDVTQLYLGGMSYYCKSHK\n"
                    + "PPISFPLCANGQVFGLYKNTCVGSDNVTDFNAIATCDWTNAGDYILANTCTERLKLFAAETLKATEETFKLS\n"
                    + "YGIATVREVLSDRELHLSWEVGKPRPPLNRNYVFTGYRVTKNSKVQIGEYTFEKGDYGDAVVYRGTTTYKLN\n"
                    + "VGDYFVLTSHTVMPLSAPTLVPQEHYVRITGLYPTLNISDEFSSNVANYQKVGMQKYSTLQGPPGTGKSHFA\n"
                    + "IGLALYYPSARIVYTACSHAAVDALCEKALKYLPIDKCSRIIPARARVECFDKFKVNSTLEQYVFCTVNALP\n"
                    + "ETTADIVVFDEISMATNYDLSVVNARLRAKHYVYIGDPAQLPAPRTLLTKGTLEPEYFNSVCRLMKTIGPDM\n"
                    + "FLGTCRRCPAEIVDTVSALVYDNKLKAHKDKSAQCFKMFYKGVITHDVSSAINRPQIGVVREFLTRNPAWRK\n"
                    + "AVFISPYNSQNAVASKILGLPTQTVDSSQGSEYDYVIFTQTTETAHSCNVNRFNVAITRAKVGILCIMSDRD\n"
                    + "LYDKLQFTSLEIPRRNVATLQAENVTGLFKDCSKVITGLHPTQAPTHLSVDTKFKTEGLCVDIPGIPKDMTY\n"
                    + "RRLISMMGFKMNYQVNGYPNMFITREEAIRHVRAWIGFDVEGCHATREAVGTNLPLQLGFSTGVNLVAVPTG\n"
                    + "YVDTPNNTDFSRVSAKPPPGDQFKHLIPLMYKGLPWNVVRIKIVQMLSDTLKNLSDRVVFVLWAHGFELTSM\n"
                    + "KYFVKIGPERTCCLCDRRATCFSTASDTYACWHHSIGFDYVYNPFMIDVQQWGFTGNLQSNHDLYCQVHGNA\n"
                    + "HVASCDAIMTRCLAVHECFVKRVDWTIEYPIIGDELKINAACRKVQHMVVKAALLADKFPVLHDIGNPKAIK\n"
                    + "CVPQADVEWKFYDAQPCSDKAYKIEELFYSYATHSDKFTDGVCLFWNCNVDRYPANSIVCRFDTRVLSNLNL\n"
                    + "PGCDGGSLYVNKHAFHTPAFDKSAFVNLKQLPFFYYSDSPCESHGKQVVSDIDYVPLKSATCITRCNLGGAV\n"
                    + "CRHHANEYRLYLDAYNMMISAGFSLWVYKQFDTYNLWNTFTRLQSLENVAFNVVNKGHFDGQQGEVPVSIIN\n"
                    + "NTVYTKVDGVDVELFENKTTLPVNVAFELWAKRNIKPVPEVKILNNLGVDIAANTVIWDYKRDAPAHISTIG\n"
                    + "VCSMTDIAKKPTETICAPLTVFFDGRVDGQVDLFRNARNGVLITEGSVKGLQPSVGPKQASLNGVTLIGEAV\n"
                    + "KTQFNYYKKVDGVVQQLPETYFTQSRNLQEFKPRSQMEIDFLELAMDEFIERYKLEGYAFEHIVYGDFSHSQ\n"
                    + "LGGLHLLIGLAKRFKESPFELEDFIPMDSTVKNYFITDAQTGSSKCVCSVIDLLLDDFVEIIKSQDLSVVSK\n"
                    + "VVKVTIDYTEISFMLWCKDGHVETFYPKLQSSQAWQPGVAMPNLYKMQRMLLEKCDLQNYGDSATLPKGIMM\n"
                    + "NVAKYTQLCQYLNTLTLAVPYNMRVIHFGAGSDKGVAPGTAVLRQWLPTGTLLVDSDLNDFVSDADSTLIGD\n"
                    + "CATVHTANKWDLIISDMYDPKTKNVTKENDSKEGFFTYICGFIQQKLALGGSVAIKITEHSWNADLYKLMGH\n"
                    + "FAWWTAFVTNVNASSSEAFLIGCNYLGKPREQIDGYVMHANYIFWRNTNPIQLSSYSLFDMSKFPLKLRGTA\n"
                    + "VMSLKEGQINDMILSLLSKGRLIIRENNRVVISSDVLVNN");
    upSeq_r1ab.setDescription("sars2 r1ab polyprotein");
    upSeq_r1ab
            .addDBRef(new DBRefEntry("UNIPROT", "0", "P0DTD1", null, true));
    upSeq_r1ab.createDatasetSequence();
    upSeq_fer1_maize = new Sequence("FER1_MAIZE",
            "MATVLGSPRAPAFFFSSSSLRAAPAPTAVALPAAKVGIMGRSASSRRRLRAQATYNVKLITPEGE"
                    + "VELQVPDDVYILDQAEEDGIDLPYSCRAGSCSSCAGKVVSGSVDQSDQSYLDDGQIADGWVLTCHAYPTSDV"
                    + "VIETHKEEELTGA");
    upSeq_fer1_maize.setDescription("Feredoxin 1 Maize");
    upSeq_fer1_maize
            .addDBRef(new DBRefEntry("UNIPROT", "0", "P27787", null, true));
    upSeq_fer1_maize.createDatasetSequence();

  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception
  {
    seq = null;
    upSeq = null;
    upSeq_r1ab = null;
    upSeq_fer1_maize = null;
  }

  @SuppressWarnings("deprecation")
  @Test(groups = { "Functional" })
  public void buildPDBQueryTest()
  {
    System.out.println("seq >>>> " + seq);

    StructureChooserQuerySource scquery = StructureChooserQuerySource
            .getQuerySourceFor(new SequenceI[]
            { seq });
    AssertJUnit
            .assertTrue(scquery instanceof PDBStructureChooserQuerySource);
    String query = scquery.buildQuery(seq);
    AssertJUnit.assertEquals("pdb_id:1tim", query);
    seq.getAllPDBEntries().clear();
    query = scquery.buildQuery(seq);
    AssertJUnit.assertEquals(
            "text:XYZ_1 OR text:XYZ_2 OR text:XYZ_3 OR text:XYZ_4 OR text:4kqy",
            query);
    seq.setDBRefs(null);
    query = scquery.buildQuery(seq);
    System.out.println(query);
    AssertJUnit.assertEquals("text:4kqy", query);

    DBRefEntry uniprotDBRef = new DBRefEntry();
    uniprotDBRef.setAccessionId("P12345");
    uniprotDBRef.setSource(DBRefSource.UNIPROT);
    seq.addDBRef(uniprotDBRef);

    DBRefEntry pdbDBRef = new DBRefEntry();
    pdbDBRef.setAccessionId("1XYZ");
    pdbDBRef.setSource(DBRefSource.PDB);
    seq.addDBRef(pdbDBRef);

    for (int x = 1; x < 5; x++)
    {
      DBRefEntry dbRef = new DBRefEntry();
      dbRef.setAccessionId("XYZ_" + x);
      seq.addDBRef(dbRef);
    }
    System.out.println("");
    System.out.println(seq.getDBRefs());
    System.out.println(query);
    query = scquery.buildQuery(seq);
    AssertJUnit.assertEquals(
            "uniprot_accession:P12345 OR uniprot_id:P12345 OR pdb_id:1xyz",
            query);
  }

  @SuppressWarnings("deprecation")
  @Test(groups = { "Functional" })
  public void buildThreeDBQueryTest()
  {
    System.out.println("seq >>>> " + upSeq);
    TDBeaconsFTSRestClientTest.setMock();
    PDBFTSRestClientTest.setMock();
    StructureChooserQuerySource scquery = StructureChooserQuerySource
            .getQuerySourceFor(new SequenceI[]
            { upSeq });
    // gets the lightweight proxy rather than the
    // ThreeDBStructureChooserQuerySource
    AssertJUnit.assertTrue(
            scquery instanceof ThreeDBStructureChooserQuerySource);
    String query = scquery.buildQuery(upSeq);
    AssertJUnit.assertEquals("P38398", query);

    // query shouldn't change regardless of additional entries
    // because 3DBeacons requires canonical entries.
    upSeq.getAllPDBEntries().clear();
    query = scquery.buildQuery(upSeq);
    AssertJUnit.assertEquals("P38398", query);
    upSeq.setDBRefs(null);
    query = scquery.buildQuery(upSeq);
    /*
     * legacy projects/datasets will not have canonical flags set for uniprot dbrefs
     * graceful behaviour would be to
     *  - pick one ? not possible
     *  - iterate through all until a 200 is obtained ?
     *  ---> ideal but could be costly
     *  ---> better to do a direct retrieval from uniprot to work out which is the canonical identifier..
     *  ----> need a test to check that accessions can be promoted to canonical!
     */
    // FIXME - need to be able to use ID to query here ?
    AssertJUnit.assertEquals(null, query);

    // TODO:
    /**
     * set of sequences: - no protein -> TDB not applicable, query PDBe only
     * (consider RNA or DNA - specific query adapter ?) - protein but no uniprot
     * -> first consider trying to get uniprot refs (need a mark to say none are
     * available) - protein and uniprot - no canonicals -> resolve to uniprot
     * automatically to get canonicals - query uniprot against 3DBeacons -->
     * decorate experimental structures with additional data from PDBe - query
     * remaining against PDBe Ranking - 3D Beacons --> in memory ranking - no
     * need to query twice Rank by - experimental > AlphaFold -> Model - start >
     * end -> filters for -> experimental only -> experimental plus best models
     * for other regions -> "best cover" -> need to be able to select correct
     * reference (the longest one that covers all) for superposition
     */
    //
    // DBRefEntry uniprotDBRef = new DBRefEntry();
    // uniprotDBRef.setAccessionId("P12345");
    // uniprotDBRef.setSource(DBRefSource.UNIPROT);
    // upSeq.addDBRef(uniprotDBRef);
    //
    // DBRefEntry pdbDBRef = new DBRefEntry();
    // pdbDBRef.setAccessionId("1XYZ");
    // pdbDBRef.setSource(DBRefSource.PDB);
    // upSeq.addDBRef(pdbDBRef);
    //
    // for (int x = 1; x < 5; x++)
    // {
    // DBRefEntry dbRef = new DBRefEntry();
    // dbRef.setAccessionId("XYZ_" + x);
    // seq.addDBRef(dbRef);
    // }
    // System.out.println("");
    // System.out.println(seq.getDBRefs());
    // System.out.println(query);
    // query = scquery.buildQuery(seq);
    // assertEquals(
    // "uniprot_accession:P12345 OR uniprot_id:P12345 OR pdb_id:1xyz",
    // query);
  }

  @Test(groups = { "Functional" }, dataProvider = "testUpSeqs")
  public void cascadingThreeDBandPDBQuerys(SequenceI testUpSeq)
  {
    TDBeaconsFTSRestClientTest.setMock();
    PDBFTSRestClientTest.setMock();
    ThreeDBStructureChooserQuerySource tdbquery = new ThreeDBStructureChooserQuerySource();
    PDBStructureChooserQuerySource pdbquery = new PDBStructureChooserQuerySource();

    FTSRestResponse upResponse = null;
    List<FTSRestResponse> pdbResponse = null;
    // TODO test available options
    // Best coverage
    // Best Alphafold Model
    // Best model (by confidence score)
    // Will also need to develop a more sophisticated filtering system
    List<FilterOption> opts = tdbquery
            .getAvailableFilterOptions(StructureChooser.VIEWS_FILTER);
    FilterOption opt_singlebest = opts.get(0);
    FilterOption opt_manybest = opts.get(1);
    assertEquals(opt_singlebest.getValue(),
            ThreeDBStructureChooserQuerySource.FILTER_FIRST_BEST_COVERAGE);
    assertEquals(opt_manybest.getValue(),
            ThreeDBStructureChooserQuerySource.FILTER_TDBEACONS_COVERAGE);

    try
    {
      upResponse = tdbquery.fetchStructuresMetaData(testUpSeq,
              tdbquery.getDocFieldPrefs().getStructureSummaryFields(),
              opt_singlebest, false);
      tdbquery.updateAvailableFilterOptions(StructureChooser.VIEWS_FILTER,
              opts, upResponse.getSearchSummary());
      // test ranking without additional PDBe data
      FTSRestResponse firstRanked = tdbquery.selectFirstRankedQuery(
              testUpSeq, upResponse.getSearchSummary(),
              tdbquery.getDocFieldPrefs().getStructureSummaryFields(),
              opt_singlebest.getValue(), false);
      assertEquals(firstRanked.getNumberOfItemsFound(), 1);
      // many best response
      upResponse = tdbquery.fetchStructuresMetaData(testUpSeq,
              tdbquery.getDocFieldPrefs().getStructureSummaryFields(),
              opt_manybest, false);
      assertTrue(firstRanked.getSearchSummary().size() < upResponse
              .getSearchSummary().size());
      // NB Could have race condition here
      List<String> pdb_Queries = tdbquery.buildPDBFTSQueryFor(upResponse);
      assertTrue(pdb_Queries.size() > 0);
      for (String pdb_Query : pdb_Queries)
      {
        assertTrue(pdb_Query.trim().length() > 0);
      }

      pdbResponse = tdbquery.fetchStructuresMetaDataFor(pdbquery,
              upResponse);
      // check all queries resulted in a response
      assertEquals(pdbResponse.size(), pdb_Queries.size());
      for (FTSRestResponse pdbr : pdbResponse)
      {
        assertTrue(pdbr.getNumberOfItemsFound() > 0);
      }

      // and finally that join works
      FTSRestResponse joinedResp = tdbquery.joinResponses(upResponse,
              pdbResponse);
      assertEquals(upResponse.getNumberOfItemsFound(),
              joinedResp.getNumberOfItemsFound());

    } catch (

    Exception x)
    {
      x.printStackTrace();
      Assert.fail("Unexpected Exception");
    }

    StructureChooserQuerySource scquery = StructureChooserQuerySource
            .getQuerySourceFor(new SequenceI[]
            { testUpSeq });

  }

  @DataProvider(name = "testUpSeqs")
  public Object[][] testUpSeqs() throws Exception
  {
    setUp();
    return new Object[][] { { upSeq }, { upSeq_insulin }, { upSeq_r1ab },
        { upSeq_fer1_maize } };
  }

  @Test(groups = { "Functional" })
  public void sanitizeSeqNameTest()
  {
    String name = "ab_cdEF|fwxyz012349";
    AssertJUnit.assertEquals(name,
            PDBStructureChooserQuerySource.sanitizeSeqName(name));

    // remove a [nn] substring
    name = "abcde12[345]fg";
    AssertJUnit.assertEquals("abcde12fg",
            PDBStructureChooserQuerySource.sanitizeSeqName(name));

    // remove characters other than a-zA-Z0-9 | or _
    name = "ab[cd],.\t£$*!- \\\"@:e";
    AssertJUnit.assertEquals("abcde",
            PDBStructureChooserQuerySource.sanitizeSeqName(name));

    name = "abcde12[345a]fg";
    AssertJUnit.assertEquals("abcde12345afg",
            PDBStructureChooserQuerySource.sanitizeSeqName(name));
  }
}
