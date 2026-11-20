package br.edu.utfpr.td.tsi.service;

import br.edu.utfpr.td.tsi.exception.MedicoNaoEncontradoException;
import br.edu.utfpr.td.tsi.model.Medico;
import br.edu.utfpr.td.tsi.repository.MedicoRepository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class MedicoService {

    private static final Logger log = LoggerFactory.getLogger(MedicoService.class);

    private static final int LOTE_TAMANHO = 1000;

    private final MedicoRepository repository;
    private final MongoTemplate mongoTemplate;

    public MedicoService(MedicoRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    private Gson criarGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
                    @Override
                    public LocalDate deserialize(JsonElement json, Type typeOfT,
                                                 JsonDeserializationContext context)
                            throws JsonParseException {
                        if (json == null || json.isJsonNull()) return null;
                        String valor = json.getAsString().trim();
                        if (valor.isEmpty()) return null;
                        if (valor.contains("/")) {
                            return LocalDate.parse(valor,
                                    DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        }
                        return LocalDate.parse(valor, DateTimeFormatter.ISO_LOCAL_DATE);
                    }
                })
                .create();
    }

    public List<Medico> cadastrar(List<Medico> medicos) {
        if (medicos == null || medicos.isEmpty()) {
            throw new IllegalArgumentException("Nenhum médico informado.");
        }

        log.debug("Recebido pedido de cadastro para {} médico(s)", medicos.size());

        for (Medico medico : medicos) {
            validar(medico);
            normalizar(medico);

            medico.setId(null);

            Optional<Medico> existente = repository.findByCrmAndUf(
                    medico.getCrm(), medico.getUf());
            if (existente.isPresent()) {
                log.warn("CRM duplicado: {}/{}", medico.getCrm(), medico.getUf());
                throw new IllegalArgumentException(
                        "CRM " + medico.getCrm() + "/" + medico.getUf()
                                + " já cadastrado.");
            }
        }

        List<Medico> salvos = repository.saveAll(medicos);
        log.info("Cadastrados {} médico(s) com sucesso", salvos.size());
        return salvos;
    }

    public List<Medico> pesquisar(String nome, String crm, String especialidade) {
        List<Criteria> filtros = new ArrayList<>();

        if (temValor(nome)) {
            String termo = nome.trim().toUpperCase();
            filtros.add(Criteria.where("NM_MEDICO").regex(Pattern.quote(termo), "i"));
        }

        if (temValor(crm)) {
            filtros.add(Criteria.where("NU_CRM").is(crm.trim().toUpperCase()));
        }

        if (temValor(especialidade)) {
            String termo = especialidade.trim().toUpperCase();
            filtros.add(Criteria.where("ESPECIALIDADE").regex(Pattern.quote(termo), "i"));
        }

        if (filtros.isEmpty()) {
            log.debug("Pesquisa sem filtros — retornando lista vazia");
            return List.of();
        }

        Criteria criteria = new Criteria().andOperator(filtros.toArray(new Criteria[0]));
        List<Medico> resultado = mongoTemplate.find(new Query(criteria), Medico.class);

        log.debug("Pesquisa [nome={}, crm={}, esp={}] → {} resultado(s)",
                nome, crm, especialidade, resultado.size());

        return resultado;
    }

    public Medico buscarPorId(String id) {
        log.debug("Buscando médico por ID: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Médico não encontrado: {}", id);
                    return new MedicoNaoEncontradoException(id);
                });
    }

    public Medico atualizar(String id, Medico novosDados) {
        log.debug("Atualizando médico ID: {}", id);

        Medico medico = buscarPorId(id);

        validar(novosDados);
        normalizar(novosDados);

        Optional<Medico> existente = repository.findByCrmAndUf(
                novosDados.getCrm(), novosDados.getUf());

        if (existente.isPresent() && !existente.get().getId().equals(id)) {
            log.warn("CRM duplicado na atualização: {}/{}",
                    novosDados.getCrm(), novosDados.getUf());
            throw new IllegalArgumentException(
                    "CRM " + novosDados.getCrm() + "/" + novosDados.getUf()
                            + " já cadastrado.");
        }

        medico.setNome(novosDados.getNome());
        medico.setNomeSocial(novosDados.getNomeSocial());
        medico.setCrm(novosDados.getCrm());
        medico.setCrmNatural(novosDados.getCrmNatural());
        medico.setUf(novosDados.getUf());
        medico.setCodigoSituacao(novosDados.getCodigoSituacao());
        medico.setSituacao(novosDados.getSituacao());
        medico.setDataInscricao(novosDados.getDataInscricao());
        medico.setCodigoTipoInscricao(novosDados.getCodigoTipoInscricao());
        medico.setTipoInscricao(novosDados.getTipoInscricao());
        medico.setEspecialidade(novosDados.getEspecialidade());
        medico.setPrimeiraInscricaoUf(novosDados.getPrimeiraInscricaoUf());
        medico.setPeriodoInicial(novosDados.getPeriodoInicial());
        medico.setPeriodoFinal(novosDados.getPeriodoFinal());
        medico.setObservacaoInterdicao(novosDados.getObservacaoInterdicao());
        medico.setInstituicaoGraduacao(novosDados.getInstituicaoGraduacao());
        medico.setAnoGraduacao(novosDados.getAnoGraduacao());
        medico.setTipoFormacao(novosDados.getTipoFormacao());
        medico.setFaculdadeEstrangeira(novosDados.getFaculdadeEstrangeira());
        medico.setPossuiPosGraduacao(novosDados.getPossuiPosGraduacao());

        Medico salvo = repository.save(medico);
        log.info("Médico atualizado: {} ({})", salvo.getNome(), id);
        return salvo;
    }

    public void deletar(String id) {
        log.debug("Deletando médico ID: {}", id);
        Medico medico = buscarPorId(id);
        repository.delete(medico);
        log.info("Médico deletado: {} ({})", medico.getNome(), id);
    }

    public long contar() {
        return repository.count();
    }

    public void importarArquivo(String nomeArquivo) {
        Gson gson = criarGson();

        log.info("Iniciando importação do arquivo: {}", nomeArquivo);

        try {
            ClassPathResource resource = new ClassPathResource(nomeArquivo);
            try (Reader reader = new InputStreamReader(
                    resource.getInputStream(), StandardCharsets.UTF_8)) {

                Type tipo = new TypeToken<List<Medico>>() {}.getType();
                List<Medico> medicos = gson.fromJson(reader, tipo);

                if (medicos == null || medicos.isEmpty()) {
                    log.warn("Arquivo {} vazio ou inválido", nomeArquivo);
                    return;
                }

                log.info("JSON carregado: {} médicos", medicos.size());

                if (log.isDebugEnabled()) {
                    for (int i = 0; i < Math.min(3, medicos.size()); i++) {
                        Medico m = medicos.get(i);
                        log.debug("DEBUG #{} → Nome: {} | CRM: {} | UF: {} | ID: {}",
                                (i + 1), m.getNome(), m.getCrm(), m.getUf(), m.getId());
                    }
                }

                int novos = 0;
                int duplicados = 0;
                int erros = 0;

                List<Medico> lote = new ArrayList<>(LOTE_TAMANHO);

                for (Medico medico : medicos) {
                    try {
                        normalizar(medico);

                        if (!temValor(medico.getCrm()) || !temValor(medico.getUf())) {
                            erros++;
                            log.warn("Registro inválido (CRM/UF nulos): nome={}, crm={}, uf={}",
                                    medico.getNome(), medico.getCrm(), medico.getUf());
                            continue;
                        }

                        Optional<Medico> existente = repository.findByCrmAndUf(
                                medico.getCrm(), medico.getUf());

                        if (existente.isPresent()) {
                            duplicados++;
                            continue;
                        }

                        medico.setId(null);
                        lote.add(medico);
                        novos++;

                        if (lote.size() >= LOTE_TAMANHO) {
                            repository.saveAll(lote);
                            log.info("Lote salvo. Novos: {} | Duplicados: {} | Erros: {}",
                                    novos, duplicados, erros);
                            lote.clear();
                        }

                    } catch (Exception e) {
                        erros++;
                        log.error("Erro ao importar registro (CRM={}, Nome={})",
                                medico.getCrm(), medico.getNome(), e);
                    }
                }

                if (!lote.isEmpty()) {
                    repository.saveAll(lote);
                    log.info("Último lote salvo.");
                }

            }
        } catch (Exception e) {
            log.error("ERRO CRÍTICO ao importar {}", nomeArquivo, e);
            throw new RuntimeException("Erro ao importar " + nomeArquivo, e);
        }
    }

    private void validar(Medico medico) {
        if (medico == null) throw new IllegalArgumentException("Médico não informado.");
        if (!temValor(medico.getNome())) throw new IllegalArgumentException("Nome é obrigatório.");
        if (!temValor(medico.getCrm())) throw new IllegalArgumentException("CRM é obrigatório.");
        if (!temValor(medico.getUf())) throw new IllegalArgumentException("UF é obrigatória.");
        if (!medico.getCrm().matches("\\d{1,10}")) {
            throw new IllegalArgumentException("CRM deve conter somente números.");
        }
        if (!medico.getUf().matches("AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO")) {
            throw new IllegalArgumentException("UF inválida.");
        }
    }

    private void normalizar(Medico medico) {
        medico.setNome(upper(medico.getNome()));
        medico.setNomeSocial(upper(medico.getNomeSocial()));
        medico.setCrm(upper(medico.getCrm()));
        medico.setCrmNatural(upper(medico.getCrmNatural()));
        medico.setUf(upper(medico.getUf()));
        medico.setCodigoSituacao(upper(medico.getCodigoSituacao()));
        medico.setSituacao(upper(medico.getSituacao()));
        medico.setCodigoTipoInscricao(upper(medico.getCodigoTipoInscricao()));
        medico.setTipoInscricao(upper(medico.getTipoInscricao()));
        medico.setEspecialidade(limparEspecialidade(medico.getEspecialidade()));
        medico.setPrimeiraInscricaoUf(upper(medico.getPrimeiraInscricaoUf()));
        medico.setObservacaoInterdicao(upper(medico.getObservacaoInterdicao()));
        medico.setInstituicaoGraduacao(upper(medico.getInstituicaoGraduacao()));
        medico.setTipoFormacao(upper(medico.getTipoFormacao()));
        medico.setFaculdadeEstrangeira(upper(medico.getFaculdadeEstrangeira()));
    }

    private String limparEspecialidade(String valor) {
        if (valor == null || valor.isBlank()) return null;

        String limpo = valor.trim();
        if (limpo.startsWith("&")) limpo = limpo.substring(1);
        limpo = limpo.replace("&", " / ");
        limpo = limpo.replaceAll("\\s+", " ");
        return limpo.toUpperCase().trim();
    }

    private String upper(String valor) {
        if (valor == null) return null;
        return valor.trim().toUpperCase();
    }

    private boolean temValor(String valor) {
        return valor != null && !valor.isBlank();
    }
}