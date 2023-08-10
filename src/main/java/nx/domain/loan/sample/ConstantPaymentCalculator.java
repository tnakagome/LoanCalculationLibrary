package nx.domain.loan.sample;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import nx.domain.loan.model.LoanInfo;
import nx.domain.loan.model.LoanInfo.PaymentType;
import nx.domain.loan.model.LoanInfo.PrepaymentType;
import nx.domain.loan.model.LoanInfo.RateType;
import nx.domain.loan.model.LoanResult;
import nx.domain.loan.model.PaymentRecord;
import nx.domain.loan.payment.AbstractPaymentTable;
import nx.domain.loan.payment.ConstantPaymentStandard;

/**
 * 元利均等の償還表を出力
 *
 * 実行方法
 * java -cp target/libloan-1.0.0-jar-with-dependencies.jar -Dfile.encoding=UTF-8 nx.domain.loan.sample.ConstantPaymentCalculator
 */
public class ConstantPaymentCalculator {
    private static long getAmount(final CommandLine options) throws Exception {
        final String amount = options.getOptionValue('a');
        if (amount == null)
            return 30_000_000;
        else
            return Long.parseLong(amount);
    }

    private static int getYears(final CommandLine options) throws Exception {
        final String years = options.getOptionValue('y');
        if (years == null)
            return (getMonths(options) > 0) ? 0 : 35;
        else
            return Integer.parseInt(years);
    }

    private static int getMonths(final CommandLine options) throws Exception {
        final String months = options.getOptionValue('m');
        if (months == null)
            return 0;
        else
            return Integer.parseInt(months);
    }

    private static double getRate(final CommandLine options) throws Exception {
        final String rate = options.getOptionValue('r');
        if (rate == null)
            return 0.01;
        else
            return Double.parseDouble(rate);
    }

    public static void main(String[] args) throws Exception {
        Options options = new Options()
        .addOption("a", true, "借入額")
        .addOption("y", true, "返済期間(年)")
        .addOption("m", true, "返済期間(月)")
        .addOption("r", true, "利率(0.01=1%)");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        }
        catch (Exception e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("ConstantPaymentCalculator", options);
            return;
        }

        long   amount       = getAmount(cmd);
        int    years        = getYears(cmd);
        int    months       = getMonths(cmd);
        double rate         = getRate(cmd);
        int    installments = years * 12 + months;
        if (installments < 1) {
            throw new IllegalArgumentException("年または月が不正です");
        }

        LoanInfo loanInfo = new LoanInfo(amount, years, months, rate, RateType.VARIABLE,
                PaymentType.CONSTANT_PAYMENT, PrepaymentType.DURATION);
        AbstractPaymentTable table = new ConstantPaymentStandard(loanInfo);
        for (PaymentRecord r : table) {
            System.out.format("%3d回  元金 %,d円  利息 %,d円  合計 %,d円  残債 %,d円\n",
                    r.getIndex() + 1, r.getPrincipal(), r.getInterest(), r.getTotal(), r.getBalance());
        }
        LoanResult result = table.getResult();
        System.out.format("\n借入期間  %d年", years);
        if (months > 0) {
            System.out.format("%d月", months);
        }
        System.out.format("\n返済方式  %s\n", loanInfo.paymentType);
        System.out.format("利率      %-3.3f%%\n", rate * 100);
        System.out.format("借入額　 %,11d円\n", loanInfo.amount);
        System.out.format("元金合計 %,11d円\n", result.getPrincipal());
        System.out.format("利息合計 %,11d円\n", result.getInterest());
        System.out.format("総支払額 %,11d円\n", result.getTotal());
        System.out.format("残債　　 %,11d円\n", result.getBalance());
    }
}
