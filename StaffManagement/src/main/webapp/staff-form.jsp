<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>${empty staff ? 'Add Staff' : 'Edit Staff'}</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
</head>
<body class="bg-light">
<div class="container mb-5">
    <h2 class="text-center mt-5 mb-4">${empty staff ? 'Add New Staff' : 'Edit Staff'}</h2>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger text-center" role="alert">
            <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <form action="staff-crud" method="post" class="shadow p-4 bg-white rounded">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <input type="hidden" name="action" value="${empty staff ? 'create' : 'update'}">

        <c:if test="${not empty staff}">
            <input type="hidden" name="staffID" value="${staff.staffID}">
        </c:if>

        <h4 class="mb-3 text-primary">Personal Information</h4>
        <div class="form-row">
            <div class="form-group col-md-6">
                <label for="fullName">Full Name</label>
                <input type="text" class="form-control" id="fullName" name="fullName" value="<c:out value='${staff.fullName}' />" required maxlength="100">
            </div>
            <div class="form-group col-md-6">
                <label for="dob">Date of Birth</label>
                <input type="date" class="form-control" id="dob" name="dob" value="<c:out value='${staff.dob}' />" required>
            </div>
        </div>

        <div class="form-row">
            <div class="form-group col-md-6">
                <label for="phoneNumber">Phone Number</label>
                <input type="text" class="form-control" id="phoneNumber" name="phoneNumber" value="<c:out value='${staff.phoneNumber}' />"
                       required maxlength="10" pattern="0[0-9]{9}" title="Phone number must be 10 digits and start with 0.">
            </div>
            <div class="form-group col-md-6">
                <label>Gender</label><br>
                <div class="form-check form-check-inline mt-2">
                    <input class="form-check-input" type="radio" name="gender" id="male" value="true" ${staff.gender ? 'checked' : ''} required>
                    <label class="form-check-label" for="male">Male</label>
                </div>
                <div class="form-check form-check-inline mt-2">
                    <input class="form-check-input" type="radio" name="gender" id="female" value="false" ${not empty staff && !staff.gender ? 'checked' : ''}>
                    <label class="form-check-label" for="female">Female</label>
                </div>
            </div>
        </div>

        <hr>
        <h4 class="mb-3 text-primary">Employment Details</h4>
        <div class="form-row">
            <div class="form-group col-md-4">
                <label for="department">Department</label>
                <input type="text" class="form-control" id="department" name="department" value="<c:out value='${staff.department}' />" required>
            </div>
            <div class="form-group col-md-4">
                <label for="position">Position</label>
                <input type="text" class="form-control" id="position" name="position" value="<c:out value='${staff.position}' />" required>
            </div>
            <div class="form-group col-md-4">
                <label for="salary">Salary ($)</label>
                <input type="number" step="0.01" class="form-control" id="salary" name="salary" value="<c:out value='${staff.salary}' />" required>
            </div>
        </div>

        <div class="form-row">
            <div class="form-group col-md-6">
                <label for="hireDate">Hire Date</label>
                <input type="date" class="form-control" id="hireDate" name="hireDate" value="<c:out value='${staff.hireDate}' />" required>
            </div>
            <div class="form-group col-md-6">
                <label>Status</label><br>
                <div class="form-check form-check-inline mt-2">
                    <input class="form-check-input" type="radio" name="isActive" id="active" value="true" ${empty staff || staff.isActive ? 'checked' : ''} required>
                    <label class="form-check-label" for="active">Active</label>
                </div>
                <div class="form-check form-check-inline mt-2">
                    <input class="form-check-input" type="radio" name="isActive" id="inactive" value="false" ${not empty staff && !staff.isActive ? 'checked' : ''}>
                    <label class="form-check-label" for="inactive">Inactive</label>
                </div>
            </div>
        </div>

        <hr>
        <h4 class="mb-3 text-primary">Account & System Role</h4>
        <div class="form-row">
            <div class="form-group col-md-4">
                <label for="email">Email</label>
                <input type="email" class="form-control" id="email" name="email" value="<c:out value='${staff.email}' />" required maxlength="100">
            </div>
            <div class="form-group col-md-4">
                <label for="roleID">System Role</label>
                <select class="form-control" id="roleID" name="roleID" required>
                    <c:forEach var="role" items="${roleList}">
                        <option value="${role.roleID}" ${staff.role.roleID == role.roleID ? 'selected' : ''}>
                            <c:out value="${role.roleName}" />
                        </option>
                    </c:forEach>
                </select>
            </div>

            <c:if test="${empty staff || staff.staffID == 0}">
                <div class="form-group col-md-4">
                    <label for="password">Password (For login)</label>
                    <input type="password" class="form-control" id="password" name="password" required maxlength="50">
                </div>
            </c:if>
        </div>

        <div class="text-center mt-4 mb-2">
            <button type="submit" class="btn btn-primary px-5 font-weight-bold">${empty staff ? 'Create Staff' : 'Update Staff'}</button>
            <a href="staff-list" class="btn btn-secondary px-5 ml-2">Cancel</a>
        </div>
    </form>
</div>
</body>
</html>