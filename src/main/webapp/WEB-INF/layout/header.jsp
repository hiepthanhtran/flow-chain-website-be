<%@page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<nav class="navbar navbar-expand-sm navbar-dark navbar-custom">
    <div class="container-fluid">
        <a class="navbar-brand navbar-custom__logo" href="<c:url value="/" />">SCMS ADMIN</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#collapsibleNavbar">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="collapsibleNavbar">
            <ul class="navbar-nav navbar-custome__menu w-100">
                <li class="nav-item navbar-custome__menu--item">
                    <a class="nav-link" href="<c:url value="/" />">Dashboard</a>
                </li>
                <s:authorize access="hasAnyRole('ADMIN')">
                    <li class="nav-item navbar-custome__menu--item">
                        <a class="nav-link" href="<c:url value="/admin/statistics" />">Thống kê</a>
                    </li>
                </s:authorize>
                <s:authorize access="!isAuthenticated()">
                    <li class="nav-item ms-auto">
                        <a class="btn btn-info" href="<c:url value="/login" />">Đăng nhập</a>
                    </li>
                </s:authorize>
                <s:authorize access="isAuthenticated()">
                    <li class="nav-item me-2 ms-auto">
                        <a class="nav-link" href="<c:url value="/" />">
                            Xin chào <s:authentication property="principal.username"/>!
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="btn btn-danger" href="<c:url value="/logout" />">Đăng xuất</a>
                    </li>
                </s:authorize>
            </ul>
        </div>
    </div>
</nav>