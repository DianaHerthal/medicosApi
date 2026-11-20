package br.edu.utfpr.td.tsi.controller;

import br.edu.utfpr.td.tsi.model.Medico;
import br.edu.utfpr.td.tsi.service.MedicoService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/medicos")
@CrossOrigin(origins = "*")
public class MedicoController {

    private static final Logger log = LoggerFactory.getLogger(MedicoController.class);

    private final MedicoService service;

    public MedicoController(MedicoService service) {
        this.service = service;
    }

    @PostMapping("/cadastrar")
    public List<Medico> cadastrar(@Valid @RequestBody List<Medico> medicos) {
        log.info("POST /medicos/cadastrar — {} médico(s)", medicos.size());
        return service.cadastrar(medicos);
    }

    @GetMapping
    public List<Medico> pesquisar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String crm,
            @RequestParam(required = false) String especialidade) {

        log.debug("GET /medicos?nome={}&crm={}&especialidade={}",
                nome, crm, especialidade);

        if (vazio(nome) && vazio(crm) && vazio(especialidade)) {
            return List.of();
        }

        return service.pesquisar(nome, crm, especialidade);
    }

    @GetMapping("/{id}")
    public Medico buscar(@PathVariable String id) {
        log.debug("GET /medicos/{}", id);
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Medico atualizar(
            @PathVariable String id,
            @RequestBody Medico medico) {
        log.info("PUT /medicos/{}", id);
        return service.atualizar(id, medico);
    }

    @DeleteMapping("/{id}")
    public String deletar(@PathVariable String id) {
        log.info("DELETE /medicos/{}", id);
        service.deletar(id);
        return "Médico deletado com sucesso!";
    }

    @PostMapping("/importar")
    public ResponseEntity<Map<String, Object>> importar() {
        log.info("POST /medicos/importar — iniciando importação");

        long antes = service.contar();

        try {
            service.importarArquivo("medicos.json");
            long depois = service.contar();

            Map<String, Object> resposta = new HashMap<>();
            resposta.put("status", "sucesso");
            resposta.put("mensagem", "Importação concluída com sucesso.");
            resposta.put("totalAntes", antes);
            resposta.put("totalDepois", depois);
            resposta.put("inseridos", depois - antes);

            log.info("Importação finalizada. Inseridos: {}", (depois - antes));
            return ResponseEntity.ok(resposta);

        } catch (Exception e) {
            log.error("Falha ao importar", e);
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("status", "erro");
            resposta.put("mensagem", e.getMessage() != null
                    ? e.getMessage() : "Erro desconhecido");
            resposta.put("totalAntes", antes);
            resposta.put("totalDepois", service.contar());
            return ResponseEntity.status(500).body(resposta);
        }
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}