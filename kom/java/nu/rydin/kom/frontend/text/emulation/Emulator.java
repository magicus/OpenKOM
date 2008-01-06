/*
 * Created on Jul 21, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.emulation;

import java.util.HashSet;
import java.util.Iterator;
import nu.rydin.kom.utils.CompoundHashMap;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */

public class Emulator
{
	private String emulationName;
	protected CompoundHashMap<String, String> associations;
	
	public static final String ACS_CHARS = "acsc";
	public static final String BACK_TAB = "cbt";
	public static final String BELL = "bel";
	public static final String CARRIAGE_RETURN = "cr";
	public static final String CHANGE_CHAR_PITCH = "cpi";
	public static final String CHANGE_LINE_PITCH = "lpi";
	public static final String CHANGE_RES_HORZ = "chr";
	public static final String CHANGE_RES_VERT = "cvr";
	public static final String CHANGE_SCROLL_REGION = "csr";
	public static final String CHAR_PADDING = "rmp";
	public static final String CLEAR_ALL_TABS = "tbc";
	public static final String CLEAR_MARGINS = "mgc";
	public static final String CLEAR_SCREEN = "clear";
	public static final String CLR_BOL = "el1";
	public static final String CLR_EOL = "el";
	public static final String CLR_EOS = "ed";
	public static final String COLUMN_ADDRESS = "hpa";
	public static final String COMMAND_CHARACTER = "cmdch";
	public static final String CREATE_WINDOW = "cwin";
	public static final String CURSOR_ADDRESS = "cup";
	public static final String CURSOR_DOWN = "cud1";
	public static final String CURSOR_HOME = "home";
	public static final String CURSOR_INVISIBLE = "civis";
	public static final String CURSOR_LEFT = "cub1";
	public static final String CURSOR_MEM_ADDRESS = "mrcup";
	public static final String CURSOR_NORMAL = "cnorm";
	public static final String CURSOR_RIGHT = "cuf1";
	public static final String CURSOR_TO_LL = "ll";
	public static final String CURSOR_UP = "cuu1";
	public static final String CURSOR_VISIBLE = "cvvis";
	public static final String DEFINE_CHAR = "defc";
	public static final String DELETE_CHARACTER = "dch1";
	public static final String DELETE_LINE = "dl1";
	public static final String DIAL_PHONE = "dl1";
	public static final String DIS_STATUS_LINE = "dsl";
	public static final String DISPLAY_CLOCK = "dclk";
	public static final String DOWN_HALF_LINE = "hd";
	public static final String ENA_ACS = "enacs";
	public static final String ENTER_ALT_CHARSET_MODE = "smacs";
	public static final String ENTER_AM_MODE = "smam";
	public static final String ENTER_BLINK_MODE = "blink";
	public static final String ENTER_BOLD_MODE = "bold";
	public static final String ENTER_CA_MODE = "smcup";
	public static final String ENTER_DELETE_MODE = "smdc";
	public static final String ENTER_DIM_MODE = "dim";
	public static final String ENTER_DOUBLEWIDE_MODE = "swidm";
	public static final String ENTER_DRAFT_QUALITY = "sdrfq";
	public static final String ENTER_INSERT_MODE = "smir";
	public static final String ENTER_ITALICS_MODE = "sitm";
	public static final String ENTER_LEFTWARD_MODE = "slm";
	public static final String ENTER_MICRO_MODE = "smicm";
	public static final String ENTER_NEAR_LETTER_QUALITY = "snlq";
	public static final String ENTER_NORMAL_QUALITY = "snrmq";
	public static final String ENTER_PROTECTED_MODE = "prot";
	public static final String ENTER_REVERSE_MODE = "rev";
	public static final String ENTER_SECURE_MODE = "invis";
	public static final String ENTER_SHADOW_MODE = "sshm";
	public static final String ENTER_STANDOUT_MODE = "smso";
	public static final String ENTER_SUBSCRIPT_MODE = "ssubm";
	public static final String ENTER_SUPERSCRIPT_MODE = "ssupm";
	public static final String ENTER_UNDERLINE_MODE = "smul";
	public static final String ENTER_UPWARD_MODE = "sum";
	public static final String ENTER_XON_MODE = "smxon";
	public static final String ERASE_CHARS = "ech";
	public static final String EXIT_ALT_CHARSET_MODE = "rmacs";
	public static final String EXIT_AM_MODE = "rmam";
	public static final String EXIT_ATTRIBUTE_MODE = "sgr0";
	public static final String EXIT_CA_MODE = "rmcup";
	public static final String EXIT_DELETE_MODE = "rmdc";
	public static final String EXIT_DOUBLEWIDE_MODE = "rwidm";
	public static final String EXIT_INSERT_MODE = "rmir";
	public static final String EXIT_ITALICS_MODE = "ritm";
	public static final String EXIT_LEFTWARD_MODE = "rlm";
	public static final String EXIT_MICRO_MODE = "rmicm";
	public static final String EXIT_SHADOW_MODE = "rshm";
	public static final String EXIT_STANDOUT_MODE = "rmso";
	public static final String EXIT_SUBSCRIPT_MODE = "rsubm";
	public static final String EXIT_SUPERSCRIPT_MODE = "rsupm";
	public static final String EXIT_UNDERLINE_MODE = "rmul";
	public static final String EXIT_UPWARD_MODE = "rum";
	public static final String EXIT_XON_MODE = "rmxon";
	public static final String FIXED_PAUSE = "pause";
	public static final String FLASH_HOOK = "hook";
	public static final String FLASH_SCREEN = "flash";
	public static final String FORM_FEED = "ff";
	public static final String FROM_STATUS_LINE = "fsl";
	public static final String GOTO_WINDOW = "wingo";
	public static final String HANGUP = "hup";
	public static final String INIT_1STRING = "is1";
	public static final String INIT_2STRING = "is2";
	public static final String INIT_3STRING = "is3";
	public static final String INIT_FILE = "if";
	public static final String INIT_PROG = "iprog";
	public static final String INITIALIZE_COLOR = "initc";
	public static final String INITIALIZE_PAIR = "initp";
	public static final String INSERT_CHARACTER = "ich1";
	public static final String INSERT_LINE = "il1";
	public static final String INSERT_PADDING = "ip";
	public static final String KEYPAD_LOCAL = "rmxk";
	public static final String KEYPAD_XMIT = "smxk";
	public static final String LAB_F0 = "lf0";
	public static final String LAB_F1 = "lf1";
	public static final String LAB_F2 = "lf2";
	public static final String LAB_F3 = "lf3";
	public static final String LAB_F4 = "lf4";
	public static final String LAB_F5 = "lf5";
	public static final String LAB_F6 = "lf6";
	public static final String LAB_F7 = "lf7";
	public static final String LAB_F8 = "lf8";
	public static final String LAB_F9 = "lf9";
	public static final String LAB_F10 = "lf10";
	public static final String LABEL_FORMAT = "fln";
	public static final String LABEL_OFF = "rmln";
	public static final String LABEL_ON = "smln";
	public static final String META_OFF = "rmm";
	public static final String META_ON = "smm";
	public static final String MICRO_COLUMN_ADDRESS = "mhpa";
	public static final String MICRO_DOWN = "mcud1";
	public static final String MICRO_LEFT = "mcub1";
	public static final String MICRO_RIGHT = "mcuf1";
	public static final String MICRO_ROW_ADDRESS = "mvpa";
	public static final String MICRO_UP = "mcuu1";
	public static final String NEWLINE = "nel";
	public static final String ORDER_OF_PINS = "porder";
	public static final String ORIG_COLORS = "oc";
	public static final String ORIG_PAIR = "op";
	public static final String PAD_CHAR = "pad";
	public static final String PARM_DCH = "dch";
	public static final String PARM_DELETE_LINE = "dl";
	public static final String PARM_DOWN_CURSOR = "cud";
	public static final String PARM_DOWN_MICRO = "mcud";
	public static final String PARM_ICH = "ich";
	public static final String PARM_INDEX = "indn";
	public static final String PARM_INSERT_LINE = "il";
	public static final String PARM_LEFT_CURSOR = "cub";
	public static final String PARM_LEFT_MICRO = "mcub";
	public static final String PARM_RIGHT_CURSOR = "cuf";
	public static final String PARM_RIGHT_MICRO = "mcuf";
	public static final String PARM_RINDEX = "rin";
	public static final String PARM_UP_CURSOR = "cuu";
	public static final String PARM_UP_MICRO = "mcuu";
	public static final String PKEY_KEY = "pfkey";
	public static final String PKEY_LOCAL = "pfloc";
	public static final String PKEY_XMIT = "pfx";
	public static final String PLAB_NORM = "pln";
	public static final String PRINT_SCREEN = "mc0";
	public static final String PRTR_NON = "mc5p";
	public static final String PRTR_OFF = "mc4";
	public static final String PRTR_ON = "mc5";
	public static final String PULSE = "pulse";
	public static final String QUICK_DIAL = "qdial";
	public static final String REMOVE_CLOCK = "rmclk";
	public static final String REPEAT_CHAR = "rep";
	public static final String REQ_FOR_INPUT = "rfi";
	public static final String RESET_1STRING = "rs1";
	public static final String RESET_2STRING = "rs2";
	public static final String RESET_3STRING = "rs3";
	public static final String RESET_FILE = "rf";
	public static final String RESTORE_CURSOR = "rc";
	public static final String ROW_ADDRESS = "vpa";
	public static final String SAVE_CURSOR = "sc";
	public static final String SCROLL_FORWARD = "ind";
	public static final String SCROLL_REVERSE = "ri";
	public static final String SELECT_CHAR_SET = "scs";
	public static final String SET_ATTRIBUTES = "sgr";
	public static final String SET_BACKGROUND = "setb";
	public static final String SET_BOTTOM_MARGIN = "smgb";
	public static final String SET_BOTTOM_MARGIN_PARM = "smgbp";
	public static final String SET_CLOCK = "sclk";
	public static final String SET_COLOR_PAIR = "scp";
	public static final String SET_FOREGROUND = "setf";
	public static final String SET_LEFT_MARGIN = "smgl";
	public static final String SET_LEFT_MARGIN_PARM = "smglp";
	public static final String SET_RIGHT_MARGIN = "smgr";
	public static final String SET_RIGHT_MARGIN_PARM = "smgrp";
	public static final String SET_TAB = "hts";
	public static final String SET_TOP_MARGIN = "smgt";
	public static final String SET_TOP_MARGIN_PARM = "smgtp";
	public static final String SET_WINDOW = "wind";
	public static final String START_BIT_IMAGE = "sbim";
	public static final String START_CHAR_SET_DEF = "scsd";
	public static final String STOP_BIT_IMAGE = "rbim";
	public static final String STOP_CHAR_SET_DEF = "rcsd";
	public static final String SUBSCRIPT_CHARACTERS = "subcs";
	public static final String SUPERSCRIPT_CHARACTERS = "supcs";
	public static final String TAB = "ht";
	public static final String THESE_CAUSE_CR = "docr";
	public static final String TO_STATUS_LINE = "tsl";
	public static final String TONE = "tone";
	public static final String UNDERLINE_CHAR = "uc";
	public static final String UP_HALF_LINE = "hu";
	public static final String USER0 = "u0";
	public static final String USER1 = "u1";
	public static final String USER2 = "u2";
	public static final String USER3 = "u3";
	public static final String USER4 = "u4";
	public static final String USER5 = "u5";
	public static final String USER6 = "u6";
	public static final String USER7 = "u7";
	public static final String USER8 = "u8";
	public static final String USER9 = "u9";
	public static final String WAIT_TONE = "wait";
	public static final String XOFF_CHARACTER = "xoffc";
	public static final String XON_CHARACTER = "xonc";
	public static final String ZERO_MOTION = "zerom";
	
	// These capabilities are defined in SVR4, but not documented in the man page.
	//
	public static final String ALT_SCANCODE_ESC = "scesa";
	public static final String BIT_IMAGE_CARRIAGE_RETURN = "bicr";
	public static final String BIT_IMAGE_NEWLINE = "binel";
	public static final String BIT_IMAGE_REPEAT = "birep";
	public static final String CHAR_SET_NAMES = "csnm";
	public static final String CODE_SET_INIT = "csin";
	public static final String COLOR_NAMES = "colornm";
	public static final String DEFINE_BIT_IMAGE_REGION = "defbi";
	public static final String DEVICE_TYPE = "devt";
	public static final String DISPLAY_PC_CHAR = "dispc";
	public static final String END_BIT_IMAGE_REGION = "endbi";
	public static final String ENTER_PC_CHARSET_MODE = "smpch";
	public static final String ENTER_SCANCODE_MODE = "smsc";
	public static final String EXIT_PC_CHARSET_MODE = "rmpch";
	public static final String EXIT_SCANCODE_MODE = "rmsc";
	public static final String GET_MOUSE = "getm";
	public static final String KEY_MOUSE = "kmous";
	public static final String MOUSE_INFO = "minfo";
	public static final String PC_TERM_OPTIONS = "pctrm";
	public static final String PKEY_PLAB = "pfxl";
	public static final String REQ_MOUSE_POS = "reqmp";
	public static final String SCANCODE_ESCAPE = "scesc";
	public static final String SET0_DES_SEQ = "s0ds";
	public static final String SET1_DES_SEQ = "s1ds";
	public static final String SET2_DES_SEQ = "s2ds";
	public static final String SET3_DES_SEQ = "s3ds";
	public static final String SET_A_BACKGROUND = "setab";
	public static final String SET_A_FOREGROUND = "setaf";
	public static final String SET_COLOR_BAND = "setcolor";
	public static final String SET_LR_MARGIN = "smglr";
	public static final String SET_PAGE_LENGTH = "slines";
	public static final String SET_TB_MARGIN = "smgtb";
	
	// From the XSI Curses standard
	//
	public static final String ENTER_HORIZONTAL_HL_MODE = "ehhlm";
	public static final String ENTER_LEFT_HL_MODE = "elhlm";
	public static final String ENTER_LOW_HL_MODE = "elohlm";
	public static final String ENTER_RIGHT_HL_MODE = "erhlm";
	public static final String ENTER_TOP_HL_MODE = "ethlm";
	public static final String ENTER_VERTICAL_HL_MODE = "evhlm";
	public static final String SET_A_ATTRIBUTES = "sgr1";
	public static final String SET_PGLEN_INCH = "slength";
	
	// Key definitions
	//
	public static final String KEY_A1 = "ka1";
	public static final String KEY_A3 = "ka3";
	public static final String KEY_B2 = "kb2";
	public static final String KEY_BACKSPACE = "kbs";
	public static final String KEY_BEG = "kbeg";
	public static final String KEY_BTAB = "kcbt";
	public static final String KEY_C1 = "kc1";
	public static final String KEY_C3 = "kc3";
	public static final String KEY_CANCEL = "kcan";
	public static final String KEY_CATAB = "ktbc";
	public static final String KEY_CLEAR = "kclr";
	public static final String KEY_CLOSE = "kclo";
	public static final String KEY_COMMAND = "kcmd";
	public static final String KEY_COPY = "kcpy";
	public static final String KEY_CREATE = "kcrt";
	public static final String KEY_CTAB = "kctab";
	public static final String KEY_DC = "kdch1";
	public static final String KEY_dl = "kdl1";
	public static final String KEY_DOWN = "kcud1";
	public static final String KEY_EIC = "krmir";
	public static final String KEY_END = "kend";
	public static final String KEY_ENTER = "kent";
	public static final String KEY_EOL = "kel";
	public static final String KEY_EOS = "ked";
	public static final String KEY_EXIT = "kext";
	public static final String KEY_F0 = "kf0"; 
	public static final String KEY_F1 = "kf1"; 
	public static final String KEY_F2 = "kf2"; 
	public static final String KEY_F3 = "kf3"; 
	public static final String KEY_F4 = "kf4"; 
	public static final String KEY_F5 = "kf5"; 
	public static final String KEY_F6 = "kf6"; 
	public static final String KEY_F7 = "kf7"; 
	public static final String KEY_F8 = "kf8"; 
	public static final String KEY_F9 = "kf9"; 
	public static final String KEY_F10 = "kf10"; 
	public static final String KEY_F11 = "kf11"; 
	public static final String KEY_F12 = "kf12"; 
	public static final String KEY_F13 = "kf13"; 
	public static final String KEY_F14 = "kf14"; 
	public static final String KEY_F15 = "kf15"; 
	public static final String KEY_F16 = "kf16"; 
	public static final String KEY_F17 = "kf17"; 
	public static final String KEY_F18 = "kf18"; 
	public static final String KEY_F19 = "kf19"; 
	public static final String KEY_F20 = "kf20"; 
	public static final String KEY_F21 = "kf21"; 
	public static final String KEY_F22 = "kf22"; 
	public static final String KEY_F23 = "kf23"; 
	public static final String KEY_F24 = "kf24"; 
	public static final String KEY_F25 = "kf25"; 
	public static final String KEY_F26 = "kf26"; 
	public static final String KEY_F27 = "kf27"; 
	public static final String KEY_F28 = "kf28"; 
	public static final String KEY_F29 = "kf29"; 
	public static final String KEY_F30 = "kf30"; 
	public static final String KEY_F31 = "kf31"; 
	public static final String KEY_F32 = "kf32"; 
	public static final String KEY_F33 = "kf33"; 
	public static final String KEY_F34 = "kf34"; 
	public static final String KEY_F35 = "kf35"; 
	public static final String KEY_F36 = "kf36"; 
	public static final String KEY_F37 = "kf37"; 
	public static final String KEY_F38 = "kf38"; 
	public static final String KEY_F39 = "kf39"; 
	public static final String KEY_F40 = "kf40"; 
	public static final String KEY_F41 = "kf41"; 
	public static final String KEY_F42 = "kf42"; 
	public static final String KEY_F43 = "kf43"; 
	public static final String KEY_F44 = "kf44"; 
	public static final String KEY_F45 = "kf45"; 
	public static final String KEY_F46 = "kf46"; 
	public static final String KEY_F47 = "kf47"; 
	public static final String KEY_F48 = "kf48"; 
	public static final String KEY_F49 = "kf49"; 
	public static final String KEY_F50 = "kf50"; 
	public static final String KEY_F51 = "kf51"; 
	public static final String KEY_F52 = "kf52"; 
	public static final String KEY_F53 = "kf53"; 
	public static final String KEY_F54 = "kf54"; 
	public static final String KEY_F55 = "kf55"; 
	public static final String KEY_F56 = "kf56"; 
	public static final String KEY_F57 = "kf57"; 
	public static final String KEY_F58 = "kf58"; 
	public static final String KEY_F59 = "kf59"; 
	public static final String KEY_F60 = "kf60"; 
	public static final String KEY_F61 = "kf61"; 
	public static final String KEY_F62 = "kf62"; 
	public static final String KEY_F63 = "kf63"; 
	public static final String KEY_FIND = "kfnd";
	public static final String KEY_HELP = "khlp";
	public static final String KEY_HOME = "khome";
	public static final String KEY_IC = "kich1";
	public static final String KEY_IL = "kil1";
	public static final String KEY_LEFT = "kcub1";
	public static final String KEY_ll = "kll";
	public static final String KEY_MARK = "kmrk";
	public static final String KEY_MESSAGE = "kmsg";
	public static final String KEY_MOVE = "kmov";
	public static final String KEY_NEXT = "knxt";
	public static final String KEY_NPAGE = "knp";
	public static final String KEY_OPEN = "kopn";
	public static final String KEY_OPTIONS = "kopt";
	public static final String KEY_PPAGE = "kpp";
	public static final String KEY_PREVIOUS = "kprv";
	public static final String KEY_PRINT = "kprt";
	public static final String KEY_REDO = "krdo";
	public static final String KEY_REFERENCE = "kref";
	public static final String KEY_REFRESH = "krfr";
	public static final String KEY_REPLACE = "krpl";
	public static final String KEY_RESTART = "krst";
	public static final String KEY_RESUME = "kres";
	public static final String KEY_RIGHT = "kcuf1";
	public static final String KEY_SAVE = "ksav";
	public static final String KEY_SBEG = "kBEG";
	public static final String KEY_SCANCEL = "kCAN";
	public static final String KEY_SCOMMAND = "kCMD";
	public static final String KEY_SCOPY = "kCPY";
	public static final String KEY_SCREATE = "kCRT";
	public static final String KEY_SDC = "kDC";
	public static final String KEY_SDL = "kDL";
	public static final String KEY_SELECT = "kslt";
	public static final String KEY_SEND = "kEND";
	public static final String KEY_SEOL = "kEOL";
	public static final String KEY_SEXIT = "kEXT";
	public static final String KEY_SF = "kind";
	public static final String KEY_SFIND = "kFND";
	public static final String KEY_SHELP = "kHLP";
	public static final String KEY_SHOME = "kHOM";
	public static final String KEY_SIC = "kIC";
	public static final String KEY_SLEFT = "kLFT";
	public static final String KEY_SMESSAGE = "kMSG";
	public static final String KEY_SMOVE = "kMOV";
	public static final String KEY_SNEXT = "kNXT";
	public static final String KEY_SOPTIONS = "kOPT";
	public static final String KEY_SPREVIOUS = "kPRV";
	public static final String KEY_SPRINT = "kPRT";
	public static final String KEY_SR = "kri";
	public static final String KEY_SREDO = "kRDO";
	public static final String KEY_SREPLACE = "kRPL";
	public static final String KEY_SRIGHT = "kRIT";
	public static final String KEY_SRSUME = "kRES";
	public static final String KEY_SSAVE = "kSAV";
	public static final String KEY_SSUSPEND = "kSPD";
	public static final String KEY_STAB = "khts";
	public static final String KEY_SUNDO = "kUND";
	public static final String KEY_SUSPEND = "kspd";
	public static final String KEY_UNDO = "kund";
	public static final String KEY_UP = "kcuu1";

	public Emulator (String emulationName, String termInfoPath)
	{
		this.emulationName = emulationName;
		associations.putAll(new TermInfoParser(termInfoPath).getEncodingMap());
		
		// OK, we're created. Now make a list of all the characters we need to be aware of. If we're asked to decode something that doesn't start with
		// any of these characters, we short-circuit and ignore the rest. By terminfo convention, all key identifiers start with a lower-case k, so we
		// just walk the compound hash and pull out anything that matches.
		//
		HashSet<String> v = new HashSet<String>();
		Iterator<String> it = associations.keySet().iterator();
		String key;
		String value;
		while (it.hasNext())
		{
			key = it.next();
			if ('k' == key.charAt(0))
			{
				value = associations.getByKey(key).substring(0, 0);
				if (!v.contains(value))
				{
					v.add(value);
				}
			}
		}
	}
	
	public String getEmulationName()
	{
		return emulationName;
	}
	
	public String parseKeySequence (String sequence)
	{
		return associations.getByValue(sequence);
	}
	
	public String convertTerminalControl (String control)
	{
		return associations.getByKey(control);
	}
}