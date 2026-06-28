<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>Staff Details</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
</head>
<body>
<div class="container mt-5">
    <div class="card shadow">
        <div class="card-header bg-info text-white text-center">
            <h3>Staff Profile: <c:out value="${staff.fullName}"/></h3>
        </div>
        <div class="card-body">
            <div class="row mb-3">
                <div class="col-sm-3 font-weight-bold">Staff ID:</div>
                <div class="col-sm-9"><c:out value="${staff.staffID}"/></div>
            </div>
            <div class="row mb-3">
                <div class="col-sm-3 font-weight-bold">Gender:</div>
                <div class="col-sm-9"><c:out value="${staff.gender ? 'Male' : 'Female'}"/></div>
            </div>
            <div class="row mb-3">
                <div class="col-sm-3 font-weight-bold">Date of Birth:</div>
                <div class="col-sm-9"><c:out value="${staff.dob}"/></div>
            </div>
            <div class="row mb-3">
                <div class="col-sm-3 font-weight-bold">Phone Number:</div>
                <div class="col-sm-9"><c:out value="${staff.phoneNumber}"/></div>
            </div>
            <div class="row mb-3">
                <div class="col-sm-3 font-weight-bold">Email:</div>
                <div class="col-sm-9"><c:out value="${staff.email}"/></div>
            </div>
            <hr>
            <div class="row mb-3">
                <div class="col-sm-3 font-weight-bold">Department:</div>
                <div class="col-sm-9"><c:out value="${staff.department}"/></div>
            </div>
            <div class="row mb-3">
                <div class="col-sm-3 font-weight-bold">Position:</div>
                <div class="col-sm-9"><c:out value="${staff.position}"/></div>
            </div>
            <div class="row mb-3">
                <div class="col-sm-3 font-weight-bold">Salary:</div>
                <div class="col-sm-9">$<c:out value="${staff.salary}"/></div>
            </div>
            <div class="row mb-3">
                <div class="col-sm-3 font-weight-bold">Hire Date:</div>
                <div class="col-sm-9"><c:out value="${staff.hireDate}"/></div>
            </div>
            <div class="row mb-3">
                <div class="col-sm-3 font-weight-bold">System Role:</div>
                <div class="col-sm-9"><c:out value="${staff.role.roleName}"/></div>
            </div>
            <div class="row mb-3">
                <div class="col-sm-3 font-weight-bold">Status:</div>
                <div class="col-sm-9">
                    <span class="badge ${staff.isActive ? 'badge-success' : 'badge-danger'}">
                        <c:out value="${staff.isActive ? 'Active' : 'Inactive'}"/>
                    </span>
                </div>
            </div>
        </div>
        <div class="card-footer text-center">
            <a href="staff-list" class="btn btn-secondary">Back to List</a>
            <c:if test="${sessionScope.user.role.roleName == 'ADMIN' || sessionScope.user.role.roleName == 'Admin'}">
                <a href="staff-crud?action=edit&id=${staff.staffID}" class="btn btn-warning">Edit Staff</a>
            </c:if>
        </div>
    </div>
</div>
</body>
</html>