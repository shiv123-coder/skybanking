<%
    // session is already available, don't redeclare it
    if (session != null) {
        session.invalidate(); // destroy session
    }
    response.sendRedirect("login.jsp"); // redirect to login page
%>
