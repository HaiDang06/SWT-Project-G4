<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>Staff List</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
</head>
<body>
<div class="container-fluid mt-4 px-5">
    <h2 class="text-center mb-4">Staff Management</h2>

    <div class="card mb-4">
        <div class="card-body">
            <form action="staff-list" method="get" class="form-inline">
                <input type="text" name="searchId" class="form-control mr-2" placeholder="ID Nhân viên" value="<c:out value='${searchId}'/>">
                <input type="text" name="searchName" class="form-control mr-2" placeholder="Tên nhân viên" value="<c:out value='${searchName}'/>">
                <input type="text" name="searchDept" class="form-control mr-2" placeholder="Phòng ban" value="<c:out value='${searchDept}'/>">
                <button type="submit" class="btn btn-primary mr-2">Search</button>
                <a href="staff-list" class="btn btn-secondary">Clear</a>
            </form>
        </div>
    </div>

    <div class="mb-3 text-right">
        <c:if test="${sessionScope.user.role.roleName == 'ADMIN' || sessionScope.user.role.roleName == 'Admin'}">
            <a href="staff-crud?action=create" class="btn btn-success">Add New Staff</a>
        </c:if>
    </div>

    <form id="deleteForm" action="staff-crud" method="post" style="display: none;">
        <input type="hidden" name="action" value="delete">
        <input type="hidden" name="id" id="deleteId">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    </form>
    <script>
        function confirmDelete(id) {
            if (confirm('Are you sure you want to delete this staff?')) {
                document.getElementById('deleteId').value = id;
                document.getElementById('deleteForm').submit();
            }
        }
    </script>

    <table class="table table-bordered table-striped">
        <thead class="thead-dark">
        <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Department</th>
            <th>Position</th>
            <th>Email</th>
            <th>Status</th>
            <th>Actions</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="staff" items="${staffList}">
            <tr>
                <td><c:out value="${staff.staffID}"/></td>
                <td><c:out value="${staff.fullName}"/></td>
                <td><c:out value="${staff.department}"/></td>
                <td><c:out value="${staff.position}"/></td>
                <td><c:out value="${staff.email}"/></td>
                <td><span class="badge ${staff.isActive ? 'badge-success' : 'badge-danger'}"><c:out value="${staff.isActive ? 'Active' : 'Inactive'}"/></span></td>
                <td>
                    <a href="staff-crud?action=view&id=${staff.staffID}" class="btn btn-sm btn-info">View</a>

                    <c:if test="${sessionScope.user.role.roleName == 'ADMIN' || sessionScope.user.role.roleName == 'Admin'}">
                        <a href="staff-crud?action=edit&id=${staff.staffID}" class="btn btn-sm btn-warning">Edit</a>
                        <button class="btn btn-sm btn-danger" onclick="confirmDelete(${staff.staffID})">Delete</button>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <c:if test="${totalPages > 1}">
        <nav>
            <ul class="pagination justify-content-center">
                <c:forEach begin="1" end="${totalPages}" var="i">
                    <li class="page-item ${currentPage == i ? 'active' : ''}">
                        <a class="page-link" href="staff-list?page=${i}&searchId=${searchId}&searchName=${searchName}&searchDept=${searchDept}">${i}</a>
                    </li>
                </c:forEach>
            </ul>
        </nav>
    </c:if>
</div>
</body>
</html>