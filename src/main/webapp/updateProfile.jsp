<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.sql.*, com.skybanking.DBConnection" %>
<%
    Integer userId = (Integer) session.getAttribute("user_id");
    if(userId == null){
        response.sendRedirect("login.jsp");
        return;
    }

    Boolean isOtpVerified = (Boolean) session.getAttribute("isOtpVerified");
    String dbFullname="", dbUsername="", dbEmail="", dbPhone="";
    String sessionEmail = (String) session.getAttribute("profileEmailToVerify");

    try(Connection con = DBConnection.getConnection()){
        PreparedStatement ps = con.prepareStatement(
            "SELECT fullname, username, email, phone FROM users WHERE user_id=?"
        );
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        if(rs.next()){
            dbFullname = rs.getString("fullname");
            dbUsername = rs.getString("username");
            dbEmail = (sessionEmail != null) ? sessionEmail : rs.getString("email");
            dbPhone = rs.getString("phone");
        }
    } catch(Exception e){ e.printStackTrace(); }
%>
<jsp:include page="WEB-INF/components/header.jsp" />

<div class="app-layout">
    <jsp:include page="WEB-INF/components/sidebar.jsp" />

    <main class="main-content d-flex align-items-center justify-content-center py-5">
        <div class="w-100 animate-fade-up" style="max-width: 600px;">
            <div class="glass-panel p-5">
                <div class="text-center mb-4">
                    <div class="d-inline-flex align-items-center justify-content-center bg-primary bg-opacity-10 text-primary rounded-circle p-4 mb-3 shadow-sm">
                        <i class="bi bi-pencil-square fs-1"></i>
                    </div>
                    <h3 class="fw-bold text-dark">Update Profile</h3>
                    <p class="text-muted small">Ensure your contact details remain up to date</p>
                </div>

                <jsp:include page="WEB-INF/components/alerts.jsp" />

                <% if(isOtpVerified == null || !isOtpVerified){ %>
                    <!-- Step 1: Send OTP -->
                    <form action="updateProfile" method="post" class="mt-4">
                        <input type="hidden" name="action" value="sendOtp">

                        <div class="bg-light p-4 rounded-4 mb-4 border shadow-sm">
                            <h6 class="fw-bold text-dark mb-3 border-bottom pb-2">Current Contact Information</h6>
                            <div class="row g-3 small">
                                <div class="col-6 d-flex align-items-center">
                                    <i class="bi bi-person text-primary me-2 fs-5"></i>
                                    <div>
                                        <span class="d-block text-muted">Full Name</span>
                                        <strong class="text-dark"><%= dbFullname %></strong>
                                    </div>
                                </div>
                                <div class="col-6 d-flex align-items-center">
                                    <i class="bi bi-person-badge text-primary me-2 fs-5"></i>
                                    <div>
                                        <span class="d-block text-muted">Username</span>
                                        <strong class="text-dark">@<%= dbUsername %></strong>
                                    </div>
                                </div>
                                <div class="col-6 d-flex align-items-center mt-3">
                                    <i class="bi bi-envelope text-primary me-2 fs-5"></i>
                                    <div>
                                        <span class="d-block text-muted">Email</span>
                                        <strong class="text-dark text-truncate d-block" style="max-width: 150px;"><%= dbEmail %></strong>
                                    </div>
                                </div>
                                <div class="col-6 d-flex align-items-center mt-3">
                                    <i class="bi bi-telephone text-primary me-2 fs-5"></i>
                                    <div>
                                        <span class="d-block text-muted">Phone</span>
                                        <strong class="text-dark"><%= dbPhone %></strong>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="mb-4">
                            <label class="form-label text-secondary small fw-bold ms-1">NEW EMAIL TO VERIFY</label>
                            <div class="input-group input-group-lg shadow-sm">
                                <span class="input-group-text bg-white border-end-0 text-muted"><i class="bi bi-envelope"></i></span>
                                <input type="email" name="email" class="form-control border-start-0 ps-0 fw-medium" value="<%= dbEmail %>" required autofocus>
                            </div>
                            <div class="form-text mt-2"><i class="bi bi-info-circle text-primary me-1"></i> You must verify ownership via OTP before updating details.</div>
                        </div>

                        <button type="submit" class="btn btn-primary btn-lg w-100 py-3 fw-bold rounded-pill shadow-sm d-flex justify-content-center align-items-center">
                            Send OTP Verification <i class="bi bi-shield-check ms-2"></i>
                        </button>
                    </form>
                <% } else { %>
                    <!-- Step 2: Editable form after OTP verified -->
                    <div class="alert alert-success d-flex align-items-center mb-4 shadow-sm border-0 border-start border-4 border-success">
                        <i class="bi bi-patch-check-fill text-success fs-3 me-3"></i> 
                        <div>
                            <strong>Email verified successfully!</strong><br>
                            <span class="small">You may now proceed to update your account details.</span>
                        </div>
                    </div>
                    
                    <form action="updateProfile" method="post" class="mt-4">
                        <input type="hidden" name="action" value="updateProfile">
                        
                        <div class="row g-3">
                            <div class="col-md-6 mb-3">
                                <label class="form-label text-secondary small fw-bold ms-1">FULL NAME</label>
                                <div class="position-relative">
                                    <i class="bi bi-person position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
                                    <input type="text" name="fullname" class="form-control form-control-lg ps-5 fw-medium" value="<%= dbFullname %>" required>
                                </div>
                            </div>
                            <div class="col-md-6 mb-3">
                                <label class="form-label text-secondary small fw-bold ms-1">USERNAME</label>
                                <div class="position-relative">
                                    <i class="bi bi-person-badge position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
                                    <input type="text" name="username" class="form-control form-control-lg ps-5 fw-medium" value="<%= dbUsername %>" required>
                                </div>
                            </div>
                            <div class="col-md-12 mb-3">
                                <label class="form-label text-secondary small fw-bold ms-1">EMAIL ADDRESS</label>
                                <div class="position-relative">
                                    <i class="bi bi-envelope position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
                                    <input type="email" name="email" class="form-control form-control-lg ps-5 fw-medium bg-light" value="<%= dbEmail %>" readonly required>
                                </div>
                            </div>
                            <div class="col-md-12 mb-4">
                                <label class="form-label text-secondary small fw-bold ms-1">PHONE NUMBER</label>
                                <div class="position-relative">
                                    <i class="bi bi-telephone position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
                                    <input type="text" name="phone" class="form-control form-control-lg ps-5 fw-medium" value="<%= dbPhone %>" required>
                                </div>
                            </div>
                        </div>

                        <button type="submit" class="btn btn-primary btn-lg w-100 py-3 fw-bold rounded-pill shadow-sm d-flex justify-content-center align-items-center">
                            Save Changes <i class="bi bi-check2-circle ms-2 fs-5"></i>
                        </button>
                    </form>
                <% } %>

                <div class="text-center mt-4 pt-4 border-top">
                    <a href="userinfo" class="text-decoration-none text-muted fw-semibold hover-scale d-inline-block transition-all"><i class="bi bi-arrow-left me-1"></i> Back to Profile</a>
                </div>
            </div>
        </div>
    </main>
</div>

<jsp:include page="WEB-INF/components/footer.jsp" />
