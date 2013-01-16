package kukkuniiaat.client;

import java.security.MessageDigest;
import java.nio.charset.Charset;
import java.net.URL;
import java.util.*;
import java.util.regex.*;
import java.io.*;

import com.sun.star.lang.Locale;
import com.sun.star.linguistic2.ProofreadingResult;
import com.sun.star.linguistic2.SingleProofreadingError;

public class Client {
    protected Process flookup = null;
    protected OutputStream fl_wr = null;
    protected InputStream fl_rd = null;
    protected Pattern rx = Pattern.compile("(\\S+)");
    protected Matcher rx_m = rx.matcher("");
    protected Pattern rx_pb = Pattern.compile("^\\p{Punct}+(\\S+?)$");
    protected Matcher rx_pb_m = rx_pb.matcher("");
    protected Pattern rx_pe = Pattern.compile("^(\\S+?)\\p{Punct}+$");
    protected Matcher rx_pe_m = rx_pe.matcher("");
    protected Pattern rx_pbe = Pattern.compile("^\\p{Punct}+(\\S+?)\\p{Punct}+$");
    protected Matcher rx_pbe_m = rx_pbe.matcher("");
	protected int position = 0;
	protected boolean debug = false;

	public static void main(String args[]) throws Exception {
		System.out.println("Initializing client");

		Client temp = new Client();
		temp.debug = true;
		Locale lt = new Locale();
		lt.Language = "kl";
		lt.Country = "GL";
		lt.Variant = "UTF-8";

        System.out.println("Checking validity of illu: " + temp.isValidWord("illu"));

		ProofreadingResult error = temp.proofreadText("Oqaasileriffiup suliassai pingaarnerit kalaallit naggueqatigiillu oqaasii misissuiffigissallugit taakkulu pillugit paasissutissanik katersuinissaq.", lt, new ProofreadingResult());
		for (int x = 0; x < error.aErrors.length; x++) {
			System.out.println(error.aErrors[x].nErrorStart + ", " + error.aErrors[x].nErrorLength + ", " + error.aErrors[x].nErrorType + ", " + error.aErrors[x].aRuleIdentifier + ", " + error.aErrors[x].aShortComment + ", " + error.aErrors[x].aFullComment);
			for (int j=0 ; j<error.aErrors[x].aSuggestions.length ; j++) {
			    System.out.println("\t" + error.aErrors[x].aSuggestions[j]);
			}
		}
	}

	public Client() {
        System.err.println("os.name\t" + System.getProperty("os.name"));
        System.err.println("os.arch\t" + System.getProperty("os.arch"));

        try {
            URL url = null;
            if (System.getProperty("os.name").startsWith("Windows")) {
                url = getClass().getResource("../../lib/foma/win32/flookup.exe");
            }
            else if (System.getProperty("os.name").startsWith("Mac")) {
                url = getClass().getResource("../../lib/foma/mac/flookup");
            }
            else if (System.getProperty("os.name").startsWith("Linux")) {
                if (System.getProperty("os.arch").startsWith("x86_64") || System.getProperty("os.arch").startsWith("amd64")) {
                    url = getClass().getResource("../../lib/foma/linux64/flookup");
                }
                else {
                    url = getClass().getResource("../../lib/foma/linux32/flookup");
                }
            }
//            System.err.println(url);

            File flookup_bin = new File(url.toURI());
            if (!flookup_bin.canExecute() && !flookup_bin.setExecutable(true)) {
                throw new Exception("Foma's flookup is not executable and could not be made executable!\nTried to execute " + flookup_bin.getCanonicalPath());
            }

            File kal_foma = new File(getClass().getResource("../../lib/foma/kal.foma").toURI());
            if (!kal_foma.canRead()) {
                throw new Exception("kal.foma is not readable!");
            }

            // This extension currently only works on Windows, Mac OS X on Intel, Linux x86, and Linux x86_64/amd64.
            ProcessBuilder pb = new ProcessBuilder(flookup_bin.getAbsolutePath(), "-b", "-x", kal_foma.getAbsolutePath());
            Map<String,String> env = pb.environment();
            env.put("CYGWIN", "nodosfilewarning");
//            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
//            System.err.println(pb.command());
            flookup = pb.start();

            fl_wr = flookup.getOutputStream();
            fl_rd = flookup.getInputStream();
	    }
		catch (Exception ex) {
			showError(ex);
		}
	}

	synchronized public ProofreadingResult proofreadText(final String paraText, final Locale locale, ProofreadingResult paRes) {
		try {
			paRes.nStartOfSentencePosition = position;
			paRes.nStartOfNextSentencePosition = position + paraText.length();
			paRes.nBehindEndOfSentencePosition = paRes.nStartOfNextSentencePosition;

            ArrayList<SingleProofreadingError> errors = new ArrayList<SingleProofreadingError>();

            rx_m.reset(paraText);
            while (rx_m.find()) {
                SingleProofreadingError err = processWord(rx_m.group(), rx_m.start());
                if (err != null) {
                    errors.add(err);
                }
            }

            paRes.aErrors = errors.toArray(paRes.aErrors);
		}
		catch (final Throwable t) {
			showError(t);
			paRes.nBehindEndOfSentencePosition = paraText.length();
		}
		return paRes;
	}

    synchronized public boolean isValid(final String word) {
        if (flookup == null || fl_wr == null || fl_rd == null) {
            return false;
        }

        if (isValidWord(word)) {
//            System.err.println(word + " valid");
            return true;
        }

        String lword = word.toLowerCase();
        if (!word.equals(lword) && isValidWord(lword)) {
//            System.err.println(word + " valid as lower case");
            return true;
        }

        rx_pe_m.reset(word);
        if (rx_pe_m.matches()) {
            if (isValidWord(rx_pe_m.group(1))) {
//                System.err.println(word + " valid with ending punctuation stripped");
                return true;
            }
            if (isValidWord(rx_pe_m.group(1).toLowerCase())) {
//                System.err.println(word + " valid with ending punctuation stripped and lower cased");
                return true;
            }
        }

        rx_pb_m.reset(word);
        if (rx_pb_m.matches()) {
            if (isValidWord(rx_pb_m.group(1))) {
//                System.err.println(word + " valid with starting punctuation stripped");
                return true;
            }
            if (isValidWord(rx_pb_m.group(1).toLowerCase())) {
//                System.err.println(word + " valid with starting punctuation stripped and lower cased");
                return true;
            }
        }

        rx_pbe_m.reset(word);
        if (rx_pbe_m.matches()) {
            if (isValidWord(rx_pbe_m.group(1))) {
//                System.err.println(word + " valid with starting punctuation stripped");
                return true;
            }
            if (isValidWord(rx_pbe_m.group(1).toLowerCase())) {
//                System.err.println(word + " valid with starting punctuation stripped and lower cased");
                return true;
            }
        }
        return false;
    }

	protected SingleProofreadingError processWord(final String word, final int start) {
        if (debug) {
            System.err.println(word + "\t" + start);
        }

        if (isValid(word)) {
            return null;
        }

        SingleProofreadingError err = new SingleProofreadingError();
        err.nErrorStart = start;
        err.nErrorLength = word.length();
        err.nErrorType = 1;
        return err;
	}

    public boolean isValidWord(String word) {
        word = word + "\n";
        byte[] res = new byte[4];
        try {
//            System.err.println("Sending data " + word);
            fl_wr.write(word.getBytes(Charset.forName("UTF-8")));
            fl_wr.flush();
//            System.err.println("Sent data " + word);
            if (fl_rd.read(res, 0, 4) != 4) {
                throw new Exception("Failed to read first 4 bytes from flookup!");
            }
//            System.err.println("Read data " + new String(res, Charset.forName("UTF-8")));
            int avail = fl_rd.available();
            byte[] res2 = new byte[4+avail];
            System.arraycopy(res, 0, res2, 0, 4);
            res = res2;
            if (fl_rd.read(res2, 4, avail) != avail) {
                throw new Exception("Failed to read first 4 bytes from flookup!");
            }
//            System.err.println("Read data " + new String(res, Charset.forName("UTF-8")));
        }
		catch (Exception ex) {
			showError(ex);
            return false;
		}

        return res[0] != '+' || res[1] != '?' || res[2] != '\n';
    }

	static void showError(final Throwable e) {
	    kukkuniiaat.openoffice.Main.showError(e);
	}

    public static String makeHash(byte[] convertme) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        }
		catch (final Throwable t) {
		}
        try {
            md = MessageDigest.getInstance("MD5");
        }
		catch (final Throwable t) {
		}
        return byteArray2Hex(md.digest(convertme));
    }

    private static String byteArray2Hex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}
