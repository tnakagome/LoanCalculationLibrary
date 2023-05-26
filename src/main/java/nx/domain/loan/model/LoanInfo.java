package nx.domain.loan.model;

/**
 * 借り入れの初期条件
 */
public class LoanInfo {
    /**
     * 借入額
     */
    public final long amount;

    /**
     * 返済期間のうち年数
     */
    public final int years;

    /**
     * 返済期間のうち月数
     */
    public final int months;

    /**
     * 返済回数
     */
    public final int installments;

    /**
     * 年率
     */
    public final double annualRate;

    /**
     * 金利種別
     */
    public enum RateType {
        /**
         * 変動金利
         */
        VARIABLE,
        /**
         * 固定金利
         */
        FIXED
    };

    /**
     * 金利種別
     */
    public final RateType rateType;

    /**
     * 返済種別
     */
    public enum PaymentType {
        /**
         * 元利均等
         */
        CONSTANT_PAYMENT,
        /**
         * 元金均等
         */
        EQUAL_PRINCIPAL_PAYMENT
    };

    /**
     * 返済種別
     */
    public final PaymentType paymentType;

    /**
     * 繰り上げ返済方式
     */
    public enum PrepaymentType {
        /** 
         * 返済期間短縮型
         */
        DURATION,
        /**
         * 返済額軽減型
         */
        AMOUNT
    };

    /**
     * 繰り上げ返済方式
     */
    public final PrepaymentType prepaymentType;

    public LoanInfo(final long amount, final int years, final int months,
                    final double annualRate, final RateType rateType,
                    final PaymentType paymentType,
                    final PrepaymentType prepaymentType) {
        if (amount <= 0 || years < 0 || months < 0 || annualRate < 0.0)
            throw new IllegalArgumentException("bad argument");
        this.amount         = amount;
        this.years          = years;
        this.months         = months;
        this.installments   = years * 12 + months;
        if (this.installments == 0)
            throw new IllegalArgumentException("The number of payment is zero.");
        this.annualRate     = annualRate;
        this.rateType       = rateType;
        this.paymentType    = paymentType;
        this.prepaymentType = prepaymentType;
    }
}
