package com.selco.auth.events;

import java.time.LocalDateTime;

/**
 * Evento para solicitar o envio de email
 * Usado para comunicar com o microserviço de email
 */
public class EmailParaEnvio {
    
    private String destinatario;
    private String assunto;
    private String corpo;
    private String tipoEmail; // CADASTRO, LOGIN_SUCESSO, LOGIN_FALHA, etc.
    private LocalDateTime dataEnvio;
    private String templateId; // Para templates de email específicos
    
    // Dados adicionais para templates
    private String nomeUsuario;
    private String senhaTemporaria;
    private String linkReset;
    
    // Construtor padrão
    public EmailParaEnvio() {}
    
    // Construtor para email de cadastro
    public static EmailParaEnvio cadastroFuncionario(String destinatario, String nomeUsuario, String senhaTemporaria) {
        EmailParaEnvio email = new EmailParaEnvio();
        email.setDestinatario(destinatario);
        email.setAssunto("Bem-vindo ao SELCO - Suas credenciais de acesso");
        email.setTipoEmail("CADASTRO");
        email.setDataEnvio(LocalDateTime.now());
        email.setNomeUsuario(nomeUsuario);
        email.setSenhaTemporaria(senhaTemporaria);
        email.setTemplateId("cadastro-funcionario");
        
        // Corpo do email
        email.setCorpo(construirCorpoCadastro(nomeUsuario, destinatario, senhaTemporaria));
        
        return email;
    }
    
    // Construtor para email de notificação de login
    public static EmailParaEnvio notificacaoLogin(String destinatario, String nomeUsuario, boolean sucesso, String ipAddress) {
        EmailParaEnvio email = new EmailParaEnvio();
        email.setDestinatario(destinatario);
        email.setTipoEmail(sucesso ? "LOGIN_SUCESSO" : "LOGIN_FALHA");
        email.setDataEnvio(LocalDateTime.now());
        email.setNomeUsuario(nomeUsuario);
        email.setTemplateId(sucesso ? "login-sucesso" : "login-falha");
        
        if (sucesso) {
            email.setAssunto("SELCO - Acesso realizado com sucesso");
            email.setCorpo(construirCorpoLoginSucesso(nomeUsuario, ipAddress));
        } else {
            email.setAssunto("SELCO - Tentativa de acesso não autorizada");
            email.setCorpo(construirCorpoLoginFalha(nomeUsuario, ipAddress));
        }
        
        return email;
    }
    
    // Métodos auxiliares para construir o corpo dos emails
    private static String construirCorpoCadastro(String nome, String email, String senhaTemporaria) {
        return String.format("""
            Olá %s,
            
            Bem-vindo ao Sistema SELCO!
            
            Seu cadastro foi realizado com sucesso. Aqui estão suas credenciais de acesso:
            
            Email: %s
            Senha temporária: %s
            
            ⚠️ IMPORTANTE: Por favor, faça o login e altere sua senha na primeira oportunidade.
            
            Link de acesso: https://selco.com.br/login
            
            Se você não solicitou este cadastro, entre em contato conosco imediatamente.
            
            Atenciosamente,
            Equipe SELCO
            """, nome, email, senhaTemporaria);
    }
    
    private static String construirCorpoLoginSucesso(String nome, String ipAddress) {
        return String.format("""
            Olá %s,
            
            Detectamos um acesso bem-sucedido em sua conta SELCO:
            
            Data/Hora: %s
            IP de origem: %s
            
            Se este acesso foi realizado por você, pode ignorar este email.
            Se não foi você, entre em contato conosco imediatamente.
            
            Atenciosamente,
            Equipe SELCO
            """, nome, LocalDateTime.now(), ipAddress);
    }
    
    private static String construirCorpoLoginFalha(String nome, String ipAddress) {
        return String.format("""
            Olá %s,
            
            ⚠️ ALERTA DE SEGURANÇA
            
            Detectamos uma tentativa de acesso não autorizada em sua conta SELCO:
            
            Data/Hora: %s
            IP de origem: %s
            
            Se esta tentativa não foi feita por você, recomendamos:
            1. Alterar sua senha imediatamente
            2. Verificar se mais alguém tem acesso às suas credenciais
            3. Entrar em contato conosco se houver suspeitas
            
            Atenciosamente,
            Equipe de Segurança SELCO
            """, nome, LocalDateTime.now(), ipAddress);
    }

    // Getters e Setters
    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getCorpo() {
        return corpo;
    }

    public void setCorpo(String corpo) {
        this.corpo = corpo;
    }

    public String getTipoEmail() {
        return tipoEmail;
    }

    public void setTipoEmail(String tipoEmail) {
        this.tipoEmail = tipoEmail;
    }

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(LocalDateTime dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getSenhaTemporaria() {
        return senhaTemporaria;
    }

    public void setSenhaTemporaria(String senhaTemporaria) {
        this.senhaTemporaria = senhaTemporaria;
    }

    public String getLinkReset() {
        return linkReset;
    }

    public void setLinkReset(String linkReset) {
        this.linkReset = linkReset;
    }

    @Override
    public String toString() {
        return "EmailParaEnvio{" +
                "destinatario='" + destinatario + '\'' +
                ", assunto='" + assunto + '\'' +
                ", tipoEmail='" + tipoEmail + '\'' +
                ", dataEnvio=" + dataEnvio +
                ", templateId='" + templateId + '\'' +
                ", nomeUsuario='" + nomeUsuario + '\'' +
                ", senhaTemporaria='[HIDDEN]'" +
                '}';
    }
}
