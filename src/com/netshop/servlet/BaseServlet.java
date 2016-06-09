package com.netshop.servlet;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName: BaseServlet
 * @Description: BaseServlet������Ϊ����Servlet�ĸ��� һ������������������ÿ��������������ԭ����service��ͬ��
 *               ԭ�� = ����ֵ���� + �������� + �����б�
 * @date ����ʱ�䣺2016��4��1�� ����6:12:44 @version=1.0
 */
@SuppressWarnings("serial")
public class BaseServlet extends HttpServlet {
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");// ������Ӧ����

		/**
		 * 1. ��ȡmethod�����������û�����õķ��� 2. �ѷ������Ʊ��Method���ʵ������ 3. ͨ��invoke()�������������
		 */
		String methodName = request.getParameter("method");
		Method method = null;
		/**
		 * 2. ͨ���������ƻ�ȡMethod����
		 */
		try {
			method = this.getClass().getMethod(methodName, HttpServletRequest.class, HttpServletResponse.class);
		} catch (Exception e) {
			throw new RuntimeException("��Ҫ���õķ�����" + methodName + "�������ڣ�", e);
		}

		/**
		 * 3. ͨ��method������������
		 */
		try {
			String result = (String) method.invoke(this, request, response);
			if (result != null && !result.trim().isEmpty()) {// ����������������ز�Ϊ��
				int index = result.indexOf(":");// ��ȡ��һ��ð�ŵ�λ��
				if (index == -1) {// ���û��ð�ţ�ʹ��ת��
					request.getRequestDispatcher(result).forward(request, response);
				} else {// �������ð��
					String start = result.substring(0, index);// �ָ��ǰ׺
					String path = result.substring(index + 1);// �ָ��·��
					if (start.equals("f")) {// ǰ׺Ϊf��ʾת��
						request.getRequestDispatcher(path).forward(request, response);
					} else if (start.equals("r")) {// ǰ׺Ϊr��ʾ�ض���
						response.sendRedirect(request.getContextPath() + path);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}