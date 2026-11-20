package br.edu.utfpr.td.tsi.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;

@Document(collection = "medicos")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Medico {

    @Id
    @JsonProperty(value = "id", access = JsonProperty.Access.READ_ONLY)
    private String id;

    @SerializedName("NM_MEDICO")
    @JsonProperty("NM_MEDICO")
    @Field("NM_MEDICO")
    @NotBlank(message = "Nome é obrigatório.")
    @Size(min = 3, max = 150)
    private String nome;

    @SerializedName("NU_CRM")
    @JsonProperty("NU_CRM")
    @Field("NU_CRM")
    @NotBlank(message = "CRM é obrigatório.")
    @Pattern(regexp = "\\d{1,10}", message = "CRM deve conter somente números.")
    private String crm;

    @SerializedName("NU_CRM_NATURAL")
    @JsonProperty("NU_CRM_NATURAL")
    @Field("NU_CRM_NATURAL")
    private String crmNatural;

    @SerializedName("SG_UF")
    @JsonProperty("SG_UF")
    @Field("SG_UF")
    @NotBlank(message = "UF é obrigatória.")
    @Pattern(regexp = "AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO")
    private String uf;

    @SerializedName("NM_SOCIAL")
    @JsonProperty("NM_SOCIAL")
    @Field("NM_SOCIAL")
    private String nomeSocial;

    @SerializedName("COD_SITUACAO")
    @JsonProperty("COD_SITUACAO")
    @Field("COD_SITUACAO")
    private String codigoSituacao;

    @SerializedName("SITUACAO")
    @JsonProperty("SITUACAO")
    @Field("SITUACAO")
    private String situacao;

    @SerializedName("DT_INSCRICAO")
    @JsonProperty("DT_INSCRICAO")
    @Field("DT_INSCRICAO")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataInscricao;

    @SerializedName("IN_TIPO_INSCRICAO")
    @JsonProperty("IN_TIPO_INSCRICAO")
    @Field("IN_TIPO_INSCRICAO")
    private String codigoTipoInscricao;

    @SerializedName("TIPO_INSCRICAO")
    @JsonProperty("TIPO_INSCRICAO")
    @Field("TIPO_INSCRICAO")
    private String tipoInscricao;

    @SerializedName("ESPECIALIDADE")
    @JsonProperty("ESPECIALIDADE")
    @Field("ESPECIALIDADE")
    @NotBlank(message = "CRM é obrigatório.")
    private String especialidade;

    @SerializedName("PRIM_INSCRICAO_UF")
    @JsonProperty("PRIM_INSCRICAO_UF")
    @Field("PRIM_INSCRICAO_UF")
    private String primeiraInscricaoUf;

    @SerializedName("PERIODO_I")
    @JsonProperty("PERIODO_I")
    @Field("PERIODO_I")
    private String periodoInicial;

    @SerializedName("PERIODO_F")
    @JsonProperty("PERIODO_F")
    @Field("PERIODO_F")
    private String periodoFinal;

    @SerializedName("OBS_INTERDICAO")
    @JsonProperty("OBS_INTERDICAO")
    @Field("OBS_INTERDICAO")
    private String observacaoInterdicao;

    @SerializedName("NM_INSTITUICAO_GRADUACAO")
    @JsonProperty("NM_INSTITUICAO_GRADUACAO")
    @Field("NM_INSTITUICAO_GRADUACAO")
    private String instituicaoGraduacao;

    @SerializedName("DT_GRADUACAO")
    @JsonProperty("DT_GRADUACAO")
    @Field("DT_GRADUACAO")
    private String anoGraduacao;

    @SerializedName("ID_TIPO_FORMACAO")
    @JsonProperty("ID_TIPO_FORMACAO")
    @Field("ID_TIPO_FORMACAO")
    private String tipoFormacao;

    @SerializedName("NM_FACULDADE_ESTRANGEIRA_GRADUACAO")
    @JsonProperty("NM_FACULDADE_ESTRANGEIRA_GRADUACAO")
    @Field("NM_FACULDADE_ESTRANGEIRA_GRADUACAO")
    private String faculdadeEstrangeira;

    @SerializedName("HAS_POS_GRADUACAO")
    @JsonProperty("HAS_POS_GRADUACAO")
    @Field("HAS_POS_GRADUACAO")
    private String possuiPosGraduacao;


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCrm() { return crm; }
    public void setCrm(String crm) { this.crm = crm; }

    public String getCrmNatural() { return crmNatural; }
    public void setCrmNatural(String crmNatural) { this.crmNatural = crmNatural; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public String getNomeSocial() { return nomeSocial; }
    public void setNomeSocial(String nomeSocial) { this.nomeSocial = nomeSocial; }

    public String getCodigoSituacao() { return codigoSituacao; }
    public void setCodigoSituacao(String codigoSituacao) { this.codigoSituacao = codigoSituacao; }

    public String getSituacao() { return situacao; }
    public void setSituacao(String situacao) { this.situacao = situacao; }

    public LocalDate getDataInscricao() { return dataInscricao; }
    public void setDataInscricao(LocalDate dataInscricao) { this.dataInscricao = dataInscricao; }

    public String getCodigoTipoInscricao() { return codigoTipoInscricao; }
    public void setCodigoTipoInscricao(String codigoTipoInscricao) { this.codigoTipoInscricao = codigoTipoInscricao; }

    public String getTipoInscricao() { return tipoInscricao; }
    public void setTipoInscricao(String tipoInscricao) { this.tipoInscricao = tipoInscricao; }

    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }

    public String getPrimeiraInscricaoUf() { return primeiraInscricaoUf; }
    public void setPrimeiraInscricaoUf(String primeiraInscricaoUf) { this.primeiraInscricaoUf = primeiraInscricaoUf; }

    public String getPeriodoInicial() { return periodoInicial; }
    public void setPeriodoInicial(String periodoInicial) { this.periodoInicial = periodoInicial; }

    public String getPeriodoFinal() { return periodoFinal; }
    public void setPeriodoFinal(String periodoFinal) { this.periodoFinal = periodoFinal; }

    public String getObservacaoInterdicao() { return observacaoInterdicao; }
    public void setObservacaoInterdicao(String observacaoInterdicao) { this.observacaoInterdicao = observacaoInterdicao; }

    public String getInstituicaoGraduacao() { return instituicaoGraduacao; }
    public void setInstituicaoGraduacao(String instituicaoGraduacao) { this.instituicaoGraduacao = instituicaoGraduacao; }

    public String getAnoGraduacao() { return anoGraduacao; }
    public void setAnoGraduacao(String anoGraduacao) { this.anoGraduacao = anoGraduacao; }

    public String getTipoFormacao() { return tipoFormacao; }
    public void setTipoFormacao(String tipoFormacao) { this.tipoFormacao = tipoFormacao; }

    public String getFaculdadeEstrangeira() { return faculdadeEstrangeira; }
    public void setFaculdadeEstrangeira(String faculdadeEstrangeira) { this.faculdadeEstrangeira = faculdadeEstrangeira; }

    public String getPossuiPosGraduacao() { return possuiPosGraduacao; }
    public void setPossuiPosGraduacao(String possuiPosGraduacao) { this.possuiPosGraduacao = possuiPosGraduacao; }
}