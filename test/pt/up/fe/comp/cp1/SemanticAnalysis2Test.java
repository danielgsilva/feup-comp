package pt.up.fe.comp.cp1;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class SemanticAnalysis2Test {

    @Test
    public void VarDeclaredLocal() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/VarDeclaredLocal.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void VarDeclaredMethod() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/VarDeclaredMethod.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void VarDeclaredClassField() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/VarDeclaredClassField.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void VarDeclaredImport() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/VarDeclaredImport.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void VarDeclaredFail() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/VarDeclaredFail.jmm"));
        System.out.println(result.getReports());
        TestUtils.mustFail(result);
    }

    @Test
    public void BinOperationIntValid() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/BinOperationIntValid.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void BinOperationIntBool() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/BinOperationIntBool.jmm"));
        System.out.println(result.getReports());
        TestUtils.mustFail(result);
    }

    @Test
    public void BinOperationBoolInt() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/BinOperationBoolInt.jmm"));
        System.out.println(result.getReports());
        TestUtils.mustFail(result);
    }

    @Test
    public void BinOperationBoolValid() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/BinOperationBoolValid.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void ArithmeticArrayOperation() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/ArithmeticArrayOperation.jmm"));
        System.out.println(result.getReports());
        TestUtils.mustFail(result);
    }

    @Test
    public void ArrayAccess() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/ArrayAccess.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void ArrayAccessOnBool() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/ArrayAccessOnBool.jmm"));
        System.out.println(result.getReports());
        TestUtils.mustFail(result);
    }

    @Test
    public void ArrayAccessWithBool() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/ArrayAccessWithBool.jmm"));
        System.out.println(result.getReports());
        TestUtils.mustFail(result);
    }

    @Test
    public void AssignIntToBool() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/AssignIntToBool.jmm"));
        System.out.println(result.getReports());
        TestUtils.mustFail(result);
    }

    @Test
    public void Factorial() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/Factorial.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void ThisInStatic() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/ThisInStatic.jmm"));
        System.out.println(result.getReports());
        TestUtils.mustFail(result);
    }

    @Test
    public void ValidThis() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/ValidThis.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void intEqualsImport() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/intEqualsImport.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void ArrayIndexInvalid() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/ArrayIndexInvalid.jmm"));
        System.out.println(result.getReports());
        TestUtils.mustFail(result);
    }

    @Test
    public void miscDuplicatedFieldInvalid() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/miscDuplicatedFieldInvalid.jmm"));
        System.out.println(result.getReports());
        TestUtils.mustFail(result);
    }

    @Test
    public void miscDuplicatedMethodInvalid() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/miscDuplicatedMethodInvalid.jmm"));
        System.out.println(result.getReports());
        TestUtils.mustFail(result);
    }

    @Test
    public void miscDuplicatedImportClassInvalid(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/miscDuplicatedImportClassInvalid.jmm"));
        System.out.println(result.getReports());
        TestUtils.mustFail(result);
    }

    @Test
    public void miscDuplicatedImportInvalid(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/miscDuplicatedImportInvalid.jmm"));
        System.out.println(result.getReports());
        TestUtils.mustFail(result);
    }

    @Test
    public void miscDuplicatedLocalInvalid(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/miscDuplicatedLocalInvalid.jmm"));
        System.out.println(result.getReports());
        TestUtils.mustFail(result);
    }

    @Test
    public void miscDuplicatedParamInvalid(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/miscDuplicatedParamInvalid.jmm"));
        System.out.println(result.getReports());
        TestUtils.mustFail(result);
    }

    @Test
    public void miscLengthAsNameOk(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/miscLengthAsNameOk.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void varargsInFieldInvalid(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/varargsInFieldInvalid.jmm"));
        System.out.println(result.getReports());
        TestUtils.mustFail(result);
    }

    @Test
    public void varargsInLocalInvalid(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/varargsInLocalInvalid.jmm"));
        System.out.println(result.getReports());
        TestUtils.mustFail(result);
    }

    @Test
    public void varargsInReturnInvalid(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/varargsInReturnInvalid.jmm"));
        System.out.println(result.getReports());
        TestUtils.mustFail(result);
    }

    @Test
    public void arrayInitAccessOk(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/arrayInitAccessOk.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void ArrayInitLengthOk(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/ArrayInitLengthOk.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void arrayInitOnCall1Ok(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/arrayInitOnCall1Ok.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void arrayInitOnCall2Ok(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/arrayInitOnCall2Ok.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void arrayInitOnCall3Ok(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/arrayInitOnCall3Ok.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void arrayInitReturnOk(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/arrayInitReturnOk.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void fieldInStaticInvalid(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/fieldInStaticInvalid.jmm"));
        System.out.println(result.getReports());
        TestUtils.mustFail(result);
    }

    @Test
    public void inheritedMethodCallSimpleOk(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/inheritedMethodCallSimpleOk.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void miscFieldAccessInvalid(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/miscFieldAccessInvalid.jmm"));
        System.out.println(result.getReports());
        TestUtils.mustFail(result);
    }

    @Test
    public void varargsTooManyInvalid(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/semanticanalysis2/varargsTooManyInvalid.jmm"));
        System.out.println(result.getReports());
        TestUtils.mustFail(result);
    }

}