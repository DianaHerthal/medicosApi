const API_URL = "/medicos";

let medicoSelecionadoId = null;


document.addEventListener("DOMContentLoaded", () => {
    console.log("app.js carregado");
    configurarEventos();
    carregarPesquisaDaUrl();
});


function configurarEventos() {
    const formPesquisa = document.getElementById("formPesquisa");
    const formMedico = document.getElementById("formMedico");

    formPesquisa.addEventListener("submit", async event => {
        event.preventDefault();
        await pesquisarMedicos();
    });

    document.getElementById("btnLimparPesquisa")
        .addEventListener("click", limparPesquisa);

    document.getElementById("btnNovoMedico")
        .addEventListener("click", abrirNovoMedico);

    document.getElementById("btnImportar")
        .addEventListener("click", importarMedicos);

    formMedico.addEventListener("submit", salvarMedico);

    document.querySelectorAll("[data-fechar-modal]")
        .forEach(el => el.addEventListener("click", fecharModalMedico));

    document.querySelectorAll("[data-fechar-detalhes]")
        .forEach(el => el.addEventListener("click", fecharDetalhes));

    document.getElementById("btnCancelarExclusao")
        .addEventListener("click", fecharModalExclusao);

    document.getElementById("btnConfirmarExclusao")
        .addEventListener("click", confirmarExclusao);
}


async function importarMedicos() {
    if (!confirm("Importar médicos do arquivo JSON?\nIsso pode demorar alguns minutos.")) {
        return;
    }

    mostrar("loading");
    esconder("semResultados");
    esconder("tabelaContainer");
    esconder("mensagemErro");

    document.getElementById("contadorResultados").textContent =
        "Importando médicos... Aguarde.";

    try {
        const response = await fetch(`${API_URL}/importar`, {
            method: "POST"
        });

        if (!response.ok) {
            throw new Error(`Erro HTTP ${response.status}`);
        }

        const dados = await response.json();

        mostrarToast(
            `Importação concluída! ${dados.inseridos} médicos inseridos.`,
            "success"
        );

        document.getElementById("contadorResultados").textContent =
            `Importação concluída. Total no banco: ${dados.totalDepois}`;

    } catch (erro) {
        console.error("Erro na importação:", erro);
        mostrarErro("Erro ao importar: " + erro.message);
    } finally {
        esconder("loading");
    }
}

function carregarPesquisaDaUrl() {
    const params = new URLSearchParams(window.location.search);
    const nome = params.get("nome") || "";
    const crm = params.get("crm") || "";
    const especialidade = params.get("especialidade") || "";

    definirValor("pesquisaNome", nome);
    definirValor("pesquisaCrm", crm);
    definirValor("pesquisaEspecialidade", especialidade);

    if (nome || crm || especialidade) {
        pesquisarMedicos();
    } else {
        mostrarEstadoInicial();
    }
}

async function pesquisarMedicos() {
    const nome = pegarValor("pesquisaNome").toUpperCase();
    const crm = pegarValor("pesquisaCrm").toUpperCase();
    const especialidade = pegarValor("pesquisaEspecialidade").toUpperCase();

    if (!nome && !crm && !especialidade) {
        mostrarToast("Informe pelo menos um filtro.", "error");
        return;
    }

    const params = new URLSearchParams();
    if (nome) params.set("nome", nome);
    if (crm) params.set("crm", crm);
    if (especialidade) params.set("especialidade", especialidade);

    const query = params.toString();
    const url = `${API_URL}?${query}`;

    window.history.replaceState({}, "", `${window.location.pathname}?${query}`);

    iniciarLoading();

    try {
        const response = await fetch(url);
        if (!response.ok) throw new Error(`Erro HTTP ${response.status}`);

        const medicos = await response.json();
        renderizarResultados(medicos);

    } catch (erro) {
        console.error("Erro na pesquisa:", erro);
        mostrarErro(erro.message);
    } finally {
        esconder("loading");
    }
}



function renderizarResultados(medicos) {
    const tbody = document.getElementById("tabelaMedicos");
    tbody.innerHTML = "";

    if (!Array.isArray(medicos)) {
        mostrarErro("Resposta inválida da API.");
        return;
    }

    if (medicos.length === 0) {
        esconder("tabelaContainer");

        document.getElementById("semResultados").innerHTML = `
            <div class="empty-icon">🔎</div>
            <h3>Nenhum médico encontrado</h3>
            <p>Tente alterar os filtros.</p>
        `;

        mostrar("semResultados");
        document.getElementById("contadorResultados").textContent =
            "Nenhum resultado encontrado";
        return;
    }

    medicos.forEach(medico => {
        const id = campo(medico, "id", "id");
        const nome = campo(medico, "NM_MEDICO", "nome");
        const nomeSocial = campo(medico, "NM_SOCIAL", "nomeSocial");
        const crm = campo(medico, "NU_CRM", "crm");
        const uf = campo(medico, "SG_UF", "uf");
        const especialidade = campo(medico, "ESPECIALIDADE", "especialidade");
        const situacao = campo(medico, "SITUACAO", "situacao");
        const instituicao = campo(medico, "NM_INSTITUICAO_GRADUACAO", "instituicaoGraduacao");
        const ano = campo(medico, "DT_GRADUACAO", "anoGraduacao");

        const linha = document.createElement("tr");

        linha.innerHTML = `
            <td>
                <strong>${escapar(nome)}</strong>
                ${nomeSocial ? `<div class="medico-secundario">${escapar(nomeSocial)}</div>` : ""}
            </td>
            <td>${escapar(crm)}</td>
            <td>${escapar(uf)}</td>
            <td>${escapar(especialidade)}</td>
            <td>${badgeSituacao(situacao)}</td>
            <td>
                ${escapar(instituicao)}
                ${ano ? `<div class="medico-secundario">${escapar(ano)}</div>` : ""}
            </td>
            <td>
                <div class="table-actions">
                    <button type="button" class="icon-btn btn-ver">Ver</button>
                    <button type="button" class="icon-btn btn-editar">Editar</button>
                    <button type="button" class="icon-btn delete btn-excluir">Excluir</button>
                </div>
            </td>
        `;

        linha.querySelector(".btn-ver").addEventListener("click", () => verDetalhes(id));
        linha.querySelector(".btn-editar").addEventListener("click", () => editarMedico(id));
        linha.querySelector(".btn-excluir").addEventListener("click", () => abrirExclusao(id));

        tbody.appendChild(linha);
    });

    esconder("semResultados");
    esconder("mensagemErro");
    mostrar("tabelaContainer");

    document.getElementById("contadorResultados").textContent =
        `${medicos.length} resultado(s) encontrado(s)`;
}

function abrirNovoMedico() {
    document.getElementById("formMedico").reset();
    definirValor("medicoId", "");
    document.getElementById("tituloModal").textContent = "Novo médico";
    mostrar("modalMedico");
}

async function salvarMedico(event) {
    event.preventDefault();

    const id = pegarValor("medicoId");

    const medico = {
        NM_MEDICO: pegarValor("nome"),
        NM_SOCIAL: pegarValor("nomeSocial") || null,
        NU_CRM: pegarValor("crm"),
        NU_CRM_NATURAL: pegarValor("crmNatural"),
        SG_UF: pegarValor("uf"),
        COD_SITUACAO: pegarValor("codigoSituacao"),
        SITUACAO: pegarValor("situacao"),
        DT_INSCRICAO: pegarValor("dataInscricao") || null,
        TIPO_INSCRICAO: pegarValor("tipoInscricao"),
        ESPECIALIDADE: pegarValor("especialidade"),
        NM_INSTITUICAO_GRADUACAO: pegarValor("instituicaoGraduacao"),
        DT_GRADUACAO: pegarValor("anoGraduacao"),
        HAS_POS_GRADUACAO: pegarValor("possuiPosGraduacao")
    };

    try {
        let response;

        if (id) {
            response = await fetch(`${API_URL}/${id}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(medico)
            });
        } else {
            response = await fetch(`${API_URL}/cadastrar`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify([medico])
            });
        }

        if (!response.ok) throw new Error(await lerErro(response));

        fecharModalMedico();
        mostrarToast(
            id ? "Médico atualizado com sucesso." : "Médico cadastrado com sucesso.",
            "success"
        );

        await atualizarPesquisa();

    } catch (erro) {
        console.error(erro);
        mostrarToast(erro.message, "error");
    }
}

async function editarMedico(id) {
    try {
        const response = await fetch(`${API_URL}/${id}`);
        if (!response.ok) throw new Error("Não foi possível carregar o médico.");

        const medico = await response.json();
        preencherFormularioMedico(medico);

        document.getElementById("tituloModal").textContent = "Editar médico";
        mostrar("modalMedico");

    } catch (erro) {
        console.error(erro);
        mostrarToast(erro.message, "error");
    }
}


function preencherFormularioMedico(medico) {
    definirValor("medicoId", campo(medico, "id", "id"));
    definirValor("nome", campo(medico, "NM_MEDICO", "nome"));
    definirValor("nomeSocial", campo(medico, "NM_SOCIAL", "nomeSocial"));
    definirValor("crm", campo(medico, "NU_CRM", "crm"));
    definirValor("crmNatural", campo(medico, "NU_CRM_NATURAL", "crmNatural"));
    definirValor("uf", campo(medico, "SG_UF", "uf"));
    definirValor("situacao", campo(medico, "SITUACAO", "situacao"));
    definirValor("codigoSituacao", campo(medico, "COD_SITUACAO", "codigoSituacao"));
    definirValor("dataInscricao", campo(medico, "DT_INSCRICAO", "dataInscricao"));
    definirValor("tipoInscricao", campo(medico, "TIPO_INSCRICAO", "tipoInscricao"));
    definirValor("especialidade", campo(medico, "ESPECIALIDADE", "especialidade"));
    definirValor("instituicaoGraduacao", campo(medico, "NM_INSTITUICAO_GRADUACAO", "instituicaoGraduacao"));
    definirValor("anoGraduacao", campo(medico, "DT_GRADUACAO", "anoGraduacao"));
    definirValor("possuiPosGraduacao", campo(medico, "HAS_POS_GRADUACAO", "possuiPosGraduacao"));
}

async function verDetalhes(id) {
    try {
        const response = await fetch(`${API_URL}/${id}`);
        if (!response.ok) throw new Error("Não foi possível carregar os detalhes.");

        const medico = await response.json();

        document.getElementById("conteudoDetalhes").innerHTML = `
            <div class="details-grid">
                ${detalhe("Nome", campo(medico, "NM_MEDICO", "nome"))}
                ${detalhe("Nome social", campo(medico, "NM_SOCIAL", "nomeSocial"))}
                ${detalhe("CRM", campo(medico, "NU_CRM", "crm"))}
                ${detalhe("UF", campo(medico, "SG_UF", "uf"))}
                ${detalhe("Situação", campo(medico, "SITUACAO", "situacao"))}
                ${detalhe("Especialidade", campo(medico, "ESPECIALIDADE", "especialidade"))}
                ${detalhe("Instituição", campo(medico, "NM_INSTITUICAO_GRADUACAO", "instituicaoGraduacao"))}
                ${detalhe("Ano de graduação", campo(medico, "DT_GRADUACAO", "anoGraduacao"))}
            </div>
        `;

        mostrar("modalDetalhes");

    } catch (erro) {
        console.error(erro);
        mostrarToast(erro.message, "error");
    }
}

function abrirExclusao(id) {
    medicoSelecionadoId = id;
    mostrar("modalExcluir");
}


async function confirmarExclusao() {
    if (!medicoSelecionadoId) return;

    try {
        const response = await fetch(`${API_URL}/${medicoSelecionadoId}`, {
            method: "DELETE"
        });

        if (!response.ok) throw new Error("Não foi possível excluir o médico.");

        fecharModalExclusao();
        mostrarToast("Médico excluído com sucesso.", "success");

        await atualizarPesquisa();

    } catch (erro) {
        console.error(erro);
        mostrarToast(erro.message, "error");
    }
}
async function atualizarPesquisa() {
    if (pegarValor("pesquisaNome")
        || pegarValor("pesquisaCrm")
        || pegarValor("pesquisaEspecialidade")) {
        await pesquisarMedicos();
    }
}


function limparPesquisa() {
    document.getElementById("formPesquisa").reset();
    window.history.replaceState({}, "", window.location.pathname);
    mostrarEstadoInicial();
}


function mostrarEstadoInicial() {
    esconder("loading");
    esconder("tabelaContainer");
    esconder("mensagemErro");

    document.getElementById("tabelaMedicos").innerHTML = "";

    document.getElementById("semResultados").innerHTML = `
        <div class="empty-icon">🔎</div>
        <h3>Faça uma pesquisa</h3>
        <p>Informe nome, CRM ou especialidade para consultar médicos.</p>
    `;

    mostrar("semResultados");
    document.getElementById("contadorResultados").textContent =
        "Nenhuma pesquisa realizada";
}

function fecharModalMedico() { esconder("modalMedico"); }
function fecharDetalhes() { esconder("modalDetalhes"); }

function fecharModalExclusao() {
    esconder("modalExcluir");
    medicoSelecionadoId = null;
}


function iniciarLoading() {
    mostrar("loading");
    esconder("semResultados");
    esconder("tabelaContainer");
    esconder("mensagemErro");
    document.getElementById("contadorResultados").textContent = "Pesquisando...";
}


function mostrarErro(mensagem) {
    esconder("loading");
    esconder("semResultados");
    esconder("tabelaContainer");

    const elemento = document.getElementById("mensagemErro");
    elemento.textContent = mensagem;
    mostrar("mensagemErro");
    document.getElementById("contadorResultados").textContent = "Erro na pesquisa";
}


function mostrarToast(mensagem, tipo = "success") {
    const toast = document.getElementById("toast");
    const texto = document.getElementById("toastMensagem");

    texto.textContent = mensagem;
    toast.className = `toast ${tipo}`;
    toast.classList.remove("hidden");

    setTimeout(() => toast.classList.add("hidden"), 5000);
}


function pegarValor(id) {
    const elemento = document.getElementById(id);
    return elemento ? elemento.value.trim() : "";
}


function definirValor(id, valor) {
    const elemento = document.getElementById(id);
    if (elemento) elemento.value = valor ?? "";
}


function mostrar(id) {
    const elemento = document.getElementById(id);
    if (elemento) elemento.classList.remove("hidden");
}


function esconder(id) {
    const elemento = document.getElementById(id);
    if (elemento) elemento.classList.add("hidden");
}


function campo(objeto, nomeJson, nomeJava) {
    return objeto?.[nomeJson] ?? objeto?.[nomeJava] ?? "";
}


function badgeSituacao(situacao) {
    if (!situacao) {
        return `<span class="badge badge-neutral">Não informado</span>`;
    }

    const regular = situacao.toUpperCase().includes("REGULAR");
    return `<span class="badge ${regular ? "badge-success" : "badge-danger"}">
        ${escapar(situacao)}
    </span>`;
}


function detalhe(titulo, valor) {
    return `
        <div class="detail-item">
            <span class="detail-label">${escapar(titulo)}</span>
            <span class="detail-value">${escapar(valor)}</span>
        </div>
    `;
}


async function lerErro(response) {
    try {
        const dados = await response.json();
        return dados.message || dados.error || `Erro HTTP ${response.status}`;
    } catch {
        return `Erro HTTP ${response.status}`;
    }
}


function escapar(valor) {
    if (valor === null || valor === undefined || valor === "") return "-";

    return String(valor)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}